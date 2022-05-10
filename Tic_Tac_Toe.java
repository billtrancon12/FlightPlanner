/**
 * 
 */
package game;

import static java.lang.System.*;
import java.awt.*;
import java.util.*;
/**
 * @author billt
 *
 */
public class Tic_Tac_Toe{

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int num = 0;
		ArrayList <Integer> tiles = new ArrayList<Integer>(100);
		ArrayList <Integer> move = new ArrayList<Integer>(100);
		ArrayList<Integer> win = new ArrayList<Integer>(100);
		for(int i=0;i<=1;i++) {
			
			for(int j=0 ; j<=2;j++) {
				win.add(j+num*3);
				num++;
			}
			for(int j=0;j<=2;i++) {
				win.add(num);
				num=num+2;
			}
		}
		for(int i = 0; i<=8; i++) {
			tiles.add(i);
		}
		for(int i=0 ; i< win.size();i++) {
			out.println(win.get(i));
		}
		out.println(win.get(0));
	}
}
class Selection
{
	
}