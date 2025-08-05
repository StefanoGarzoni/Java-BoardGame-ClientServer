package it.polimi.ingsw.client.TUI.components.drawable;

import it.polimi.ingsw.client.TUI.AnsiColors;
import it.polimi.ingsw.client.TUI.TerminalBuffer;
import it.polimi.ingsw.client.TUI.TerminalCell;
import it.polimi.ingsw.client.TUI.components.Drawable;
import it.polimi.ingsw.client.TUI.components.drawable.util.BoxStyle;
import it.polimi.ingsw.client.TUI.components.drawable.util.DrawUtils;
import it.polimi.ingsw.model.Direction;

public class DrawableTimer implements Drawable {
    private TerminalCell[][] timerSprite;
    public final static int ROW_COUNT = 3;
    public final static int COL_COUNT = 9;

    public DrawableTimer(long time){
        timerSprite = new TerminalCell[ROW_COUNT][COL_COUNT];

        for(int i=0;i<ROW_COUNT;i++){
            for(int j=0;j<COL_COUNT;j++){
                timerSprite[i][j] = new TerminalCell();
            }
        }

        DrawUtils.drawBox(COL_COUNT, ROW_COUNT, timerSprite, AnsiColors.WHITE, AnsiColors.BLACK, 0, 0, BoxStyle.THICK);
        //TODO convert in MM:SS?
        if(time > 0)
            DrawUtils.drawText(String.valueOf(time) + "s", timerSprite, 1, 3, AnsiColors.WHITE, AnsiColors.BLACK, Direction.EAST);
        else
            DrawUtils.drawText("0s", timerSprite, 1, 4, AnsiColors.WHITE, AnsiColors.BLACK, Direction.EAST);

        timerSprite[0][4].setPixel(AnsiColors.CYAN, AnsiColors.BLACK, "⌛");
    }

    @Override
    public TerminalCell[][] draw() {
        return timerSprite;
    }
}
