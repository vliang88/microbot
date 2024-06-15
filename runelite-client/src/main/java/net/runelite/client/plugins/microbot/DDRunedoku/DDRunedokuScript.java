package net.runelite.client.plugins.microbot.DDRunedoku;

import net.runelite.api.NPC;
import net.runelite.api.annotations.Component;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.math.Random;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.shop.Rs2Shop;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntilOnClientThread;


public class DDRunedokuScript extends Script {
    public static double version = 1.0;
    private int AliID = 3533;
    private int sudokuBoardWidgetID = 19136524;
    public boolean readyToClick;

    public int[][] originalSudokuBoard = new int[9][9];
    public int[][] sudokuBoard = new int[9][9];
    public int[][] diffSudokuBoard = new int[9][9];
    public int boughtRuneNum = 0;
    long startTime = 0;
    public boolean run(DDRunedokuConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            try {
                if(Rs2Widget.getWidget(sudokuBoardWidgetID) == null){
                    if(Rs2Widget.hasWidget("Find out what the runes are")){
                        buyRunes();
                        System.out.println("Total time for loop " + (System.currentTimeMillis() - startTime));
                    }else {
                        startTime = System.currentTimeMillis();
                        talkToAli();
                    }
                }else{
                    readSudokuBoard(originalSudokuBoard);
                    for(int i = 0; i < originalSudokuBoard.length; i++)
                        sudokuBoard[i] = originalSudokuBoard[i].clone();
                    if(solveSudoku(sudokuBoard,0,0)){
                        //printBoard(originalSudokuBoard);
                        //printBoard(sudokuBoard);
                        //printBoard(diffSudokuBoard);
                        fillBoard(sudokuBoard);
                        checkBoard();
                        clickSubmit();
                        sleep(250,500);
                        sleepUntil(() -> Rs2Widget.hasWidget("Find out what the runes are"));
                    }
                }

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
        return true;
    }

    private void talkToAli(){
        NPC npc = Rs2Npc.getNpc(AliID);
        Rs2Npc.interact(npc, "Trade");
        sleep(250,500);
        sleepUntil(() -> Rs2Widget.hasWidget("View runes"));
        Rs2Widget.clickWidget("View runes");
        sleep(250,500);
        sleepUntil(() -> Rs2Widget.hasWidget("large casket"));
        Rs2Widget.clickWidget("large casket");
        sleep(250,500);
        sleepUntil(() -> Rs2Widget.hasWidget("continue"));
        Rs2Widget.clickWidget("continue");
        sleep(250,500);
        sleepUntil(() -> Rs2Widget.hasWidget("Examine lock"));
        Rs2Widget.clickWidget("Examine lock");
        sleep(250,500);
        sleepUntil(() -> Rs2Widget.getWidget(sudokuBoardWidgetID) != null);
    }

    private void readSudokuBoard(int[][] board){
        //Go thru the loop to fill the buffer
        if(Rs2Widget.getWidget(19136526) != null) {
            for(int r = 0; r < 9; r++){
                for(int c = 0; c < 9; c++){
                    int child = (r*9)+c;
                    String widgetName = Rs2Widget.getWidget(19136526).getChild(child).getName();
                    switch (widgetName) {
                        case "<col=ff9040>Water rune</col>":
                            board[r][c] = 1;
                            continue;
                        case "<col=ff9040>Fire rune</col>":
                            board[r][c] = 2;
                            continue;
                        case "<col=ff9040>Earth rune</col>":
                            board[r][c] = 3;
                            continue;
                        case "<col=ff9040>Air rune</col>":
                            board[r][c] = 4;
                            continue;
                        case "<col=ff9040>Mind rune</col>":
                            board[r][c] = 5;
                            continue;
                        case "<col=ff9040>Body rune</col>":
                            board[r][c] = 6;
                            continue;
                        case "<col=ff9040>Law rune</col>":
                            board[r][c] = 7;
                            continue;
                        case "<col=ff9040>Chaos rune</col>":
                            board[r][c] = 8;
                            continue;
                        case "<col=ff9040>Death rune</col>":
                            board[r][c] = 9;
                            continue;
                        default:
                            board[r][c] = 0;
                    }
                }
            }
        }
    }

    private boolean solveSudoku(int[][] board, int row, int column){
        //Make sure we dont keep going if we are at the end of the grid
        if(column == 9 && row == 8){ //We are at the very end
            return true;
        }
        if(column == 9){
            row++;
            column = 0;
        }
        if(board[row][column] > 0){
            return solveSudoku(board, row, column+1);
        }
        for(int num = 1; num <= 9; num++){
            if(isSafe(board, column, row, num)) {
                board[row][column] = num;
                if (solveSudoku(board, row, column + 1)) {
                    return true;
                }
            }
            board[row][column] = 0;
        }
        return false;
    }

    /////////RULE VALIDATION FUNCTIONS!
    private boolean isSafe(int[][] board, int row, int column, int value){
        if(!rowIsSafe(board, column, row, value))
            return false;
        if(!columnIsSafe(board, column, row, value))
            return false;
        if(!boxIsSafe(board, column, row, value))
            return false;
        return true;
    }

    private boolean rowIsSafe(int[][] board, int row, int column, int value){
        for(int c = 0; c < 9; c++){
            if(board[row][c] == value)
                return false;
        }
        return true;
    }

    private boolean columnIsSafe(int[][] board, int row, int column, int value){
        for(int r = 0; r < 9; r++){
            if(board[r][column] == value){
                return false;
            }
        }
        return true;
    }

    private boolean boxIsSafe(int[][] board, int row, int column, int value){
        int startRow = (row/3)*3;
        int startCol = (column/3)*3;
        for(int r = startRow; r < startRow+3; r++){
            for(int c = startCol; c < startCol+3; c++){
                if(board[r][c] == value) {
                    return false;
                }
            }
        }
        return true;
    }

    void fillBoard(int[][] board){
        Widget sudokuBoardWidget = Rs2Widget.getWidget(19136526);
        for(int runes = 1; runes <= 9; runes++){ //For each rune run thru the board
            //Choose the rune from the side panel
            Rs2Widget.clickChildWidget(19136521, runes);
            sleep(300,600);
            for(int row = 0; row < 9; row++){
                for(int col = 0; col < 9; col++){
                    int childId = (row*9)+col;
                    if(board[row][col] == runes && originalSudokuBoard[row][col] == 0){
                        readyToClick = false;
                        Rs2Widget.clickChildWidget(19136525,childId);
                        sleep(100,200);
                    }
                }
            }
        }
    }

    boolean checkBoard(){
        for(int r = 0; r < 9; r++){
            for(int c = 0; c < 9; c++) {
                int temp = Rs2Widget.getWidget(19136525).getChild((r*9)+c).getItemId();
                if(Rs2Widget.getWidget(19136525).getChild((r*9)+c).getItemId() == -1) {
                    //Select appropraite button on side
                    Rs2Widget.clickChildWidget(19136521, sudokuBoard[r][c]);
                    sleep(200,300);
                    //click on the slot
                    Rs2Widget.clickChildWidget(19136525, (r*9)+c);
                    sleep(100,200);
                }
            }
        }
        return true;
    }

    void clickSubmit(){
        Rs2Widget.clickChildWidget(19136522,8);
    }

    void buyRunes(){
        Rs2Widget.clickWidget("Find out what the runes are");
        sleep(100,250);
        sleepUntil(() -> Rs2Widget.hasWidget("Ali Morrisane's discount rune store."));
        Rs2Shop.buyItem("Cosmic rune", "50");
        sleep(100,250);
        sleepUntil(() -> !Rs2Shop.hasStock("Cosmic rune"));
        Rs2Shop.closeShop();
    }

    void printBoard(int[][] board){
        for(int row = 0; row < 9; row++){
            if(row%3 == 0){System.out.println("------------");};
            for(int col = 0; col < 9; col++){
                if(col%3 == 0){System.out.print("|");};
                System.out.print(board[row][col]);
            }
            System.out.println(" ");
        }
        System.out.println(" ");
        System.out.println(" ");
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}
