package com.company;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class Main extends JPanel {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private int size;//размер игрового поля
    private int nbTiles;//количество плиток = сайз на сайз-1 = 15
    private int dimension;

    private static final Color FOREGROUND_COLOR = new Color(68, 33, 57);
    private static final Random RANDOM = new Random();
    private int[] tiles;
    private int score;
    private int tileSize;//размер плиточки
    private int blankPos;//позиция пустой плиточки
    private int margin;// запас сетки
    private int gridSize;
    private boolean gameOver;

    public static void main(String[] args){
        SwingUtilities.invokeLater(()->{
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setTitle("DaGame");
            frame.setResizable(false);
            frame.add(new Main(4,550,30),BorderLayout.CENTER);
            frame.pack();

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
    public Main(int size, int dim, int mar){
        this.size=size;
        dimension=dim;
        margin=mar;

        nbTiles=size*size-1;
        tiles= new int[size*size];

        gridSize=(dim-2*margin);
        tileSize=gridSize/size;
        setPreferredSize(new Dimension(dimension, dimension+margin));
        setBackground(Color.pink);
        getForeground(FOREGROUND_COLOR);
        setFont(new Font("Monospaced",Font.BOLD,22));
        gameOver=true;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e){
                //взаимодействие с сеткой щелчком
                //
                if (gameOver){
                    newGame();
                }else{
                   // Graphics2D G2D= (Graphics2D) g;
                   // drawScoreMessage(G2D);

                    score++;
                    //System.out.println(score);
                    ourLordSavior();
                    //получить позицию клика
                    int ex = e.getX()-margin;
                    int ey = e.getY()-margin;
                    //щелчок по сетке
                    if (ex<0||ex>gridSize||ey<0||ey>gridSize){
                        return;}
                    //получить позицию в сетке
                    int c1 = ex/tileSize;
                    int r1 =ey/tileSize;
                    //получить позицию пустой ячейки
                    int c2 = blankPos%size;
                    int r2=blankPos/size;
                    // конвертация в 1Д координату
                    int clickPos = r1*size+c1;
                    int dir = 0;

                    if(c1==c2 && Math.abs(r1-r2)>0) {
                        if ((r1 - r2) > 0) dir = size;
                        else dir = -size;
                    }
                    else if (r1==r2 && Math.abs(c1-c2)>0) {
                        if ((c1 - c2) > 0) dir = 1;
                        else dir = -1;
                    }
                    if (dir!=0){
                        //перемещение плиток в направлении
                        do{
                            int newBlankPos = blankPos+dir;
                            tiles[blankPos]=tiles[newBlankPos];
                            blankPos=newBlankPos;

                        }while (blankPos!=clickPos);
                        tiles[blankPos]=0;
                    }
                    //проверка, решена ли игра
                    gameOver=isSolved();
                }
                //перекраска панели
                repaint();
            }
        });
        newGame();
    }

    private Color getForeground(Color FOREGROUND_COLOR) {
        return FOREGROUND_COLOR;
    }

    private void newGame(){
        do{
            reset();
            shuffle();
        }while (!isSolvable());
        gameOver=false;
        for (int i = 0; i <tiles.length; i++)
            System.out.println(tiles[i]);
    }
    private void reset(){
        for (int i = 0; i <tiles.length; i++){
            tiles[i]=(i+1)%tiles.length;
        }
        //перемещение пустой ячейки в последнюю позицию
        blankPos=tiles.length-1;
    }
    private void shuffle(){
        //без пустой клеточки, чтоб она осталась в конце
        int n=nbTiles;
        while (n > 1) {
            int r = RANDOM.nextInt(n--);
            int tmp = tiles[r];
            tiles[r]=tiles[n];
            tiles[n]=tmp;

        }

    }
    private boolean isSolvable(){
        int countInv=0;
        for(int i =0; i<nbTiles;i++){
            for (int j =0; j<i;j++){
                if(tiles[j]>tiles[i])
                    countInv++;
            }
        }
        return countInv%2==0;
    }
    private boolean isSolved(){
        if(tiles[tiles.length-1]!=0)//если пустая не на своей позиции, то не решено
            return false;
        for (int i = nbTiles-1;i>=0;i--){
            if(tiles[i]!=i+1)
                return false;
        }
        return true;
    }
    private void drawGrid(Graphics2D g){
        for(int i=0; i<tiles.length;i++){
            //конвертация 1Д в 2Д с учетоm размера 2Д массива
            int r = i/size;
            int c=i%size;

            int x = margin+c* tileSize;
            int y = margin+r *tileSize;
            //проверка пуcтой ячейки
            if(tiles[i]==0){
                if (gameOver){
                    g.setColor(FOREGROUND_COLOR);
                    drawCenteredString(g,"\u2713",x,y);
                }
                continue;
            }
            //для другой плитки
            g.setColor(getForeground());
            g.fillRoundRect(x,y,tileSize,tileSize,25,25);
            g.setColor(Color.BLACK);
            g.drawRoundRect(x,y,tileSize,tileSize,25,25);
            g.setColor(Color.PINK);
            drawCenteredString(g, String.valueOf(tiles[i]),x,y);
        }
    }
    private void ourLordSavior(){
        //String json = GSON.toJson(score);
        String json = GSON.toJson(tiles);
        System.out.println(json);
    }

    private void drawStartMessage(Graphics2D g){
        if(gameOver){
            g.setFont(getFont().deriveFont(Font.BOLD,29));
            g.setColor(FOREGROUND_COLOR);
            String s ="PUSsH to play again";
            g.drawString(s,(getWidth()-g.getFontMetrics().stringWidth(s))/2,
                    getHeight()-margin);
        }
    }

    private void drawCenteredString(Graphics2D g,String s, int x, int y){

        FontMetrics fm= g.getFontMetrics();
        int asc =fm.getAscent();
        int desc = fm.getDescent();
        g.drawString(s,x+(tileSize-fm.stringWidth(s))/2,
                y+(asc+(tileSize-(asc+desc))/2));
    }
    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2D= (Graphics2D) g;
//        g2D.setRenderingHints((Map<?, ?>) RenderingHints.VALUE_ANTIALIAS_ON);
        drawGrid(g2D);
        drawStartMessage(g2D);
        String scoreString;
        scoreString="score: "+score;
        g.drawString(scoreString,2,17);
    }

}
