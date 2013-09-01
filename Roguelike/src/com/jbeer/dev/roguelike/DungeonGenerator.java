package com.jbeer.dev.roguelike;

import java.util.Random;


/* Coding by Adrian Moore. June 30 1993. V1.00  *
 ************************************************
 * Probably contact through UNSW, no account    *
 * though: s2121366@orchestra.cs.unsw.oz.au     *
 * Else: PO BOX 336, Lane Cove 2066, Australia  *
 ************************************************
 * Program: DungeonMapGenV1 *
 * Purpose: Generate Dungeon maps. *
 * Status : Public Domain, retain notice of *
 *          original author. No liabilities *
 *     accepted. Use at own risk. *
 *     Modified code must be marked as *
 *     such if redistributed. Use of  *
 *     program implies agreement with *
 *     this notice. *
 *     Software or gifts greatfully  *
 *     accepted ;-) *
 * NOTE: Role players contact me and let me *
 *  know what's going on in the world! *
 ************************************************
 * eg  DungeonMapGen 30 20 5050 5 5 4 7 5 1     *
 ***********************************************/

public class DungeonGenerator
{

	static class pos
	{
		int x;
		int y;
		int dir;
	};

	static char Dungeon[][] = new char[150][150]; /* The map */
	static pos[] queue = new pos[100]; /* the remember list for past positions */

	static int queueCount = 0;

	static int curx,cury,Maxx,Maxy,Direction,Complexity,CleanUp,Spread;
	static boolean flag = true;
	static boolean debug = true;

	static int argumentToInteger(String s)
	{
		return new Integer(s).intValue();
	}

	public static void main(String[] argv)
	{
		int seed;

		if(argv.length == 0)
		{
			System.out.print("\nBy Adrian Moore (Public Domain 1993)\n");
			System.out.print("KEY: MaxX,MaxY,seed,x,y,Complexity,CleanUp,Spread,Start Direction\n\n");
			System.out.print("MaxX & MaxY : Dimension of workspace (Max 150 * 150)\n");
			System.out.print("seed : Random seed\n");
			System.out.print("x & y : Offset for starting pos\n");
			System.out.print("Complexity : Low = Building  High = Caverns\n");
			System.out.print("CleanUp : Should be about 7\n");
			System.out.print("Spread : How spread out rooms are\n");
			System.out.print("Start Direction : 1-4\n\n");
			System.out.print("Seed = 0 will force a random seed value.\n");
			System.out.print("Contact at - UNSW, s2121366@orchestra.cs.unsw.oz.au\n");
			System.out.print(" or AuSNAILPost - PO BOX 336, Lane Cove 2066, Australia\n");
		}
		else
		{
			Maxx = argumentToInteger(argv[0]);
			Maxy = argumentToInteger(argv[1]);
			seed = argumentToInteger(argv[2]);

			curx = argumentToInteger(argv[3]);
			cury = argumentToInteger(argv[4]);

			Complexity = argumentToInteger(argv[5]);
			CleanUp = argumentToInteger(argv[6]);

			Spread = argumentToInteger(argv[7]);
			Direction = argumentToInteger(argv[8]);
			if(Direction<1||Direction>4) Direction = randRoll(4);

			randRoll(seed); /* Randomise rand function */
			Initialise();
			GenerateRandomRooms(seed);
			CleanDungeon();
			PrintDungeon();
		}
	}

	static int randRoll(int die)
	{
		return ((new Random().nextInt(die)) + 1);
	}

	static void CleanDungeon()
	{
		int SurroundCount,count1,count2,vx,vy;

		for(vx = 1; vx < Maxx-1; vx++)
			for(vy = 1; vy < Maxy-1; vy++)
				if((Dungeon[vx][vy]!='*')&&(Dungeon[vx][vy]!='M')
						&&(Dungeon[vx][vy]!='X')
						&&(Dungeon[vx][vy]!='>')
						&&(Dungeon[vx][vy]!='<'))
				{
					SurroundCount=0;
					for(count1 = -1; count1 < 2; count1++)
						for(count2 = -1; count2 < 2; count2++)
							if((Dungeon[vx+count1][vy+count2]==' ')||
									(Dungeon[vx+count1][vy+count2]=='M')||
									(Dungeon[vx+count1][vy+count2]=='>')||
									(Dungeon[vx+count1][vy+count2]=='<')||
									(Dungeon[vx+count1][vy+count2]=='X')||
									(Dungeon[vx+count1][vy+count2]=='D'))
								SurroundCount++;
					if(SurroundCount>CleanUp) Dungeon[vx][vy] = ' ';
				}
	}

	static void Initialise()
	{
		int cx,cy;

		for(cx=0;cx<Maxx;cx++)
			for(cy=0;cy<Maxy;cy++)
				Dungeon[cx][cy] = '*';

		// Java - pre-allocate queued objects
		for(int i = 0; i < 100; i++)
		{
			queue[i] = new pos();
		};

	}

	static void PrintDungeon()
	{
		int cx,cy;

		BoundRect(0,0,Maxx,Maxy,'#');

		for(cy=0;cy<Maxy;cy++)
		{
			for(cx=0;cx<Maxx;cx++)
				System.out.print(Dungeon[cx][cy]);
			System.out.print("\n");
		}
	}

	static void GenerateRandomRooms(int seed)
	{
		int RoomType;

		BuildWalls(curx, cury);
		BoundDraw(curx, cury, '>'); /* Entrance stairs */
		ChangePos(1);

		while(flag)
		{
			if((curx > Maxx-2)||(cury > Maxy-2)||(curx < 2)||(cury < 2))
			{
				System.out.println("Off edge\n");
				RetrieveLast(); /* Moved off edge */
			}
			else
			{
				if((Dungeon[curx][cury]!='*')&&(Dungeon[curx][cury]!='#'))
					/*If space already mapped*/
				{
					/*System.out.print("Already Mapped\n");*/
					RetrieveLast();
				}
				RoomType=randRoll(20);
				if(RoomType<3) Corridor(Spread);
				else if(RoomType<5) Door();
				else if(RoomType<10) SidePassage();
				else if(RoomType<15) PassageTurns();
				else if(RoomType<17) Chamber();
				else if(RoomType<18) Stairs();
				else if(RoomType<19) DeadEnd();
				else if(RoomType<20) Trick();
				else Monster();
			}
//			if(debug) System.out.println("x="+curx+", y="+cury);
//			if(debug) PrintDungeon();
		}
	}

	static void BuildWalls(int x,int y)
	{
		int count1,count2;

		for(count1=-1;count1<2;count1++) /* fill area around */
			for(count2=-1;count2<2;count2++)
				BoundDraw(x+count1,y+count2,'#');
	}

	static void Door()
	{
		int dir = Direction;
		int roll=randRoll(3);

		if(roll==1) dir += 1;
		else if(roll==2) dir -= 1;
		else /* Straight ahead */
		{
			BuildWalls(curx,cury);
			BoundDraw(curx,cury,'D');
			ChangePos(1);
		}
		if(dir!=Direction)
		{
			if(dir>4) dir = 1;
			else if(dir<1) dir = 4;

			AddCurrent(curx+2*dirxChange(dir),cury+2*diryChange(dir),dir);
			BuildWalls(curx,cury);
			BoundDraw(curx+dirxChange(dir),cury+diryChange(dir),'D');
		}
	}

	static void SidePassage()
	{
		int dir = Direction;
		int roll=randRoll(2);
		if(debug) System.out.println("SidePassage.");

		if(roll==1) dir += 1;
		else dir -= 1;

		if(dir>4) dir = 1;
		else if(dir<1) dir = 4;

		AddCurrent(curx+dirxChange(dir),cury+diryChange(dir),dir);
		BuildWalls(curx,cury);
		BoundDraw(curx,cury,' ');
		ChangePos(1);
	}

	static int dirxChange(int dir)
	{
		if(dir==4) return -1;
		else if(dir==2) return 1;
		else return 0; // Added for Java.
	}

	static int diryChange(int dir)
	{
		if(dir==1) return -1;
		else if(dir==3) return 1;
		else return 0; // Added for Java.
	}

	static void PassageTurns()
	{
		int roll=randRoll(2);

		if(debug) System.out.println("PassageTurns.");

		if(roll==1) Direction += 1;
		else Direction -= 1;

		if(Direction>4) Direction = 1;
		else if(Direction<1) Direction = 4;
		BuildWalls(curx,cury);
		BoundDraw(curx,cury,' ');
		ChangePos(1);
	}

	static void Chamber()
	{
		int cx,cy,offset,count,NorthPos,WestPos,EastPos,SouthPos;
		int roll = randRoll(20);

		if(debug) System.out.println("Chamber.");

		if(roll<7) /* 2*2 chamber */
		{
			cx=6;cy=8;
		}
		else if(roll<10) /* 4*4 chamber */
		{
			cx=4;cy=4;
		}
		else if(roll<12) /* 6*6 chamber */
		{
			cx=6;cy=6;
		}
		else if(roll<14) /* 6*4 chamber */
		{
			cx=6;cy=4;
		}
		else if(roll<15) /* 4*6 chamber */
		{
			cx=4;cy=6;
		}
		else if(roll<18) /* 2*4 chamber */
		{
			cx=8;cy=4;
		}
		else
		{
			cx=4;cy=8;
		}

		if(Direction==1)
		{
			offset = randRoll(cx/2);
			WestPos=curx-offset;
			NorthPos=cury-cy+1;
			EastPos=curx+(cx-offset);
			SouthPos=cury;
			BoundRect(WestPos,NorthPos,cx,cy,'#',' ');
			BoundDraw(curx,cury,'-');
		}
		else if(Direction==2)
		{
			offset = randRoll(cy/2);
			NorthPos=cury-offset;
			WestPos=curx;
			EastPos=curx+cx-1;
			SouthPos=cury+(cy-offset);
			BoundRect(curx,NorthPos,cx,cy,'#',' ');
			BoundDraw(curx,cury,'|');
		}
		else if(Direction==3)
		{
			offset = randRoll(cx/2);
			NorthPos=cury;
			EastPos=curx+(cx-offset);
			SouthPos=cury+cy-1;
			WestPos=curx-offset;
			BoundRect(WestPos,cury,cx,cy,'#',' ');
			BoundDraw(curx,cury,'-');
		}
		else
		{
			offset = randRoll(cy/2);
			NorthPos=cury-offset;
			EastPos=curx;
			SouthPos=cury+(cx-offset);
			WestPos=curx-cx+1;
			BoundRect(curx-cx+1,NorthPos,cx,cy,'#',' ');
			BoundDraw(curx,cury,'|');
		}

		/* Do exits at some positions */

		for(count=0;count<randRoll(Complexity);count++)
		{
			roll=randRoll(4);
			if(roll==1)
			{
				offset=randRoll(EastPos-2-WestPos);
				AddCurrent(WestPos+offset,NorthPos-1,1);
				BoundDraw(WestPos+offset,NorthPos,'-');
			}
			else if(roll==2)
			{
				offset=randRoll(SouthPos-2-NorthPos);
				AddCurrent(EastPos+1,NorthPos+offset,2);
				BoundDraw(EastPos,NorthPos+offset,'|');
			}
			else if(roll==3)
			{
				offset = randRoll(EastPos-2-WestPos);
				AddCurrent(WestPos+offset,SouthPos+1,3);
				BoundDraw(WestPos+offset,SouthPos,'-');
			}
			else
			{
				offset = randRoll(SouthPos-2-NorthPos);
				AddCurrent(WestPos-1,NorthPos+offset,4);
				BoundDraw(WestPos,NorthPos+offset,'|');
			}
		}

		RetrieveLast(); /* Now work from somewhere else (possibly an exit)*/
	}

	static int RandSign()
	{
		if(randRoll(2)==1) return 1;
		else return -1;
	}

	static void Stairs()
	{
		if(debug) System.out.println("Stairs.");
		BuildWalls(curx,cury);
		if(randRoll(2)==1)
		{
			BoundDraw(curx,cury,'>');
			ChangePos(1);
		}
		else
		{
			BoundDraw(curx,cury,'<');
			ChangePos(1);
		}
	}

	static void DeadEnd()
	{
		int roll;
		roll = randRoll(12);
		if(debug) System.out.println("DeadEnd.");
		BuildWalls(curx,cury);
		BoundDraw(curx,cury,' ');
		if(roll==1) /* Secret Door */
		{
			/* Either ahead or to the side */
			roll= randRoll(3);
			if(roll==1) /* Ahead */
			{
				ChangePos(1);
				BuildWalls(curx,cury);
				BoundDraw(curx,cury,'$');
				ChangePos(1);
			}
			else if(roll==2) /* to left */
			{
				Direction -= 1;
				if(Direction<1) Direction=4;
				ChangePos(1);
				BuildWalls(curx,cury);
				BoundDraw(curx,cury,'$');
				ChangePos(1);
			}
			else
			{
				Direction += 1;
				if(Direction>4) Direction=1;
				ChangePos(1);
				BuildWalls(curx,cury);
				BoundDraw(curx,cury,'$');
				ChangePos(1);
			}
		}
		else if(roll<4) /* Concealed Door */
		{
			/* Either ahead or to the side */
			roll= randRoll(3);
			if(roll==1) /* Ahead */
			{
				ChangePos(1);
				BuildWalls(curx,cury);
				BoundDraw(curx,cury,'c');
				ChangePos(1);
			}
			else if(roll==2) /* to left */
			{
				Direction -= 1;
				if(Direction<1) Direction=4;
				ChangePos(1);
				BuildWalls(curx,cury);
				BoundDraw(curx,cury,'c');
				ChangePos(1);
			}
			else
			{
				Direction += 1;
				if(Direction>4) Direction=1;
				ChangePos(1);
				BuildWalls(curx,cury);
				BoundDraw(curx,cury,'c');
				ChangePos(1);
			}
		}
		else /* Dead end */
		{
			ChangePos(1);
			BuildWalls(curx,cury);
			BoundDraw(curx,cury,'#');
			RetrieveLast();
		}
	}

	static void AddCurrent(int vx,int vy,int dir)
	{
		/*int CountSurround = 0;
 int count1,count2;*/

		if(queueCount==99) flag = false; /* too many loose ends
 this shouldn't really exit,
 rather it should try to tie
 them off */
		else
		{
			/*for(count1=-1;count1<2;count1++)
 for(count2=-1;count2<2;count2++)
 if(Dungeon[vx+count1][vy+count2]=='*') CountSurround++;

 System.out.print("CountSurround %d queue %d\n",CountSurround,queueCount);*/
			queueCount++;
			queue[queueCount].x=vx;
			queue[queueCount].y=vy;
			queue[queueCount].dir=dir;
		}
	}

	static void RetrieveLast()
	{
		if(queueCount==0) flag = false; /* Finished. Everything is closed */
		else
		{
			curx = queue[queueCount].x;
			cury = queue[queueCount].y;
			Direction = queue[queueCount].dir;
			queueCount--;
		}
	}

	static void Trick()
	{
		if(debug) System.out.println("Trick/Trap.");
		BuildWalls(curx,cury);
		BoundDraw(curx,cury,'X');
		ChangePos(1);
	}

	static void Monster()
	{
		if(debug) System.out.println("Monster.");
		BuildWalls(curx,cury);
		BoundDraw(curx,cury,'M');
		ChangePos(1);
	}

	static void Corridor(int dist)
	{
		int count;
		if(debug) System.out.println("Corridor.");

		if(Direction==1) /* North */
			for(count=0;count<dist;count++)
			{
				BuildWalls(curx,cury-count);
				BoundDraw(curx,cury-count,' ');
			}
		else if(Direction==2) /* east */
			for(count=0;count<dist;count++)
			{
				BuildWalls(curx+count,cury);
				BoundDraw(curx+count,cury,' ');
			}
		else if(Direction==3) /* south */
			for(count=0;count<dist;count++)
			{
				BuildWalls(curx,cury+count);
				BoundDraw(curx,cury+count,' ');
			}
		else /* west */
			for(count=0;count<dist;count++)
			{
				BuildWalls(curx-count,cury);
				BoundDraw(curx-count,cury,' ');
			}
		ChangePos(dist);
	}

	static void ChangePos(int dist)
	{
		if(Direction==1) cury=cury-dist;
		else if(Direction==2) curx=curx+dist;
		else if(Direction==3) cury=cury+dist;
		else curx=curx-dist;
	}

	static void BoundDraw(int tx,int ty,char floor)
	{
		if((tx < Maxx) && (tx > -1) && (ty < Maxy) && (ty > -1))
		{ // new bounds checking - must be on map
			if((ty<(Maxy-1))&&(tx<(Maxx-1))&&(ty>0)&&(tx>0)&&((Dungeon[tx][ty]=='*')||(Dungeon[tx][ty]=='#')))
				Dungeon[tx][ty] = floor;
			else if(((ty==Maxy-1)||(tx==Maxx-1)||(ty==0)||(tx==0))&&(floor=='#'))
				Dungeon[tx][ty] = '#';
		}
	}

	static void BoundRect(int tx,int ty,int width,int height,char outer,char inner)
	{
		int count1,count2;

		for(count2=0;count2<width;count2++) /* Draw top & bottom */
		{
			BoundDraw(tx+count2,ty,outer);
			BoundDraw(tx+count2,ty+height-1,outer);
		}
		for(count1=1;count1<(height-1);count1++) /* Draw Sides */
		{
			BoundDraw(tx,ty+count1,outer);
			BoundDraw(tx+width-1,ty+count1,outer);
		}

		for(count1=1;count1<(height-1);count1++) /* fill interior */
			for(count2=1;count2<(width-1);count2++)
				BoundDraw(tx+count2,ty+count1,inner);
	}

	static void BoundRect(int tx,int ty,int width,int height,char outer)
	{
		int count1,count2;

		for(count2=0;count2<width;count2++) /* Draw top & bottom */
		{
			BoundDraw(tx+count2,ty,outer);
			BoundDraw(tx+count2,ty+height-1,outer);
		}
		for(count1=1;count1<(height-1);count1++) /* Draw Sides */
		{
			BoundDraw(tx,ty+count1,outer);
			BoundDraw(tx+width-1,ty+count1,outer);
		}
	}


}