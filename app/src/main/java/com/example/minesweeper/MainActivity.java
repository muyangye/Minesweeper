package com.example.minesweeper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.gridlayout.widget.GridLayout;

import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private int[][] mines = new int[10][8];
    private int[][] flags = new int[10][8];
    private TextView[][] grids = new TextView[10][8];
    private boolean isPick = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Add 10 * 8 grids dynamically
        GridLayout gameBoard = findViewById(R.id.gameBoard);
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 8; j++) {
                TextView tv = new TextView(this);
                tv.setHeight(dpToPixel(35));
                tv.setWidth(dpToPixel(35));
                tv.setTextSize( 32 );//dpToPixel(32) );
                tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                tv.setBackgroundColor(Color.GREEN);
                tv.setOnClickListener(this::onClickTV);

                androidx.gridlayout.widget.GridLayout.LayoutParams lp = new androidx.gridlayout.widget.GridLayout.LayoutParams();
                lp.setMargins(dpToPixel(2), dpToPixel(2), dpToPixel(2), dpToPixel(2));
                lp.rowSpec = androidx.gridlayout.widget.GridLayout.spec(i);
                lp.columnSpec = androidx.gridlayout.widget.GridLayout.spec(j);

                gameBoard.addView(tv, lp);
                grids[i][j] = tv;
                // mines/flags[i][j] = 1 means there's a mine/flag at (i, j), initially set to 0
                mines[i][j] = 0;
                flags[i][j] = 0;
            }
        }

        // Randomly generates 4 mines
        Random random = new Random();
        List<Integer> minesPos = random.ints(0, 80)
                .distinct().limit(10).boxed().collect(Collectors.toList());
        for (Integer minePos : minesPos) {
            mines[minePos % 10][minePos % 8] = 1;
        }
    }

    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    // Helper function to get the "coordinate" (i, j) of the current TextView
    private Pair<Integer, Integer> findCoordinate(TextView tv)
    {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 8; j++) {
                if (grids[i][j] == tv) {
                    return new Pair<Integer, Integer>(i, j);
                }
            }
        }
        return new Pair<Integer, Integer>(-1, -1);
    }

    private List<Pair<Integer, Integer>> findMinesCoordinates()
    {
        List<Pair<Integer, Integer>> minesCoordinates = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 8; j++) {
                if (mines[i][j] == 1) {
                    minesCoordinates.add(new Pair<>(i, j));
                }
            }
        }
        return minesCoordinates;
    }

    private List<Pair<Integer, Integer>> findFlagsCoordinates()
    {
        List<Pair<Integer, Integer>> flagsCoordinates = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 8; j++) {
                if (flags[i][j] == 1) {
                    flagsCoordinates.add(new Pair<>(i, j));
                }
            }
        }
        return flagsCoordinates;
    }

    public void onClickTV(View view)
    {
        TextView tv = (TextView) view;
        Pair<Integer, Integer> startCoordinate = findCoordinate(tv);
        if (isPick) {
            // If click on a mine
            if (mines[startCoordinate.first.intValue()][startCoordinate.second.intValue()] == 1) {
                // Display all mines
                for (Pair<Integer, Integer> mineCoordinate : findMinesCoordinates()) {
                    int row = mineCoordinate.first;
                    int col = mineCoordinate.second;
                    grids[row][col].setText(R.string.mine);
                }
                // Redirect to result page and show lost message
                Intent intent = new Intent(this, ResultActivity.class);
                startActivity(intent);
                return;
            }
            // BFS
            Queue<Pair<Integer, Integer>> queue = new LinkedList<Pair<Integer, Integer>>();
            queue.add(startCoordinate);
            Set<Pair<Integer, Integer>> visited = new HashSet<>();
            int[][] directions = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
            while (!queue.isEmpty()) {
                for (int i = 0; i < queue.size(); i++) {
                    Pair<Integer, Integer> coordinate = queue.poll();
                    int row = coordinate.first;
                    int col = coordinate.second;
                    grids[row][col].setBackgroundColor(Color.GRAY);
                    List<Pair<Integer, Integer>> okCoordinates = new ArrayList<>();
                    // Total number of possible nearby grids that aren't miness
                    // (this variable is to handle corners that don't count towards mines/safe)
                    int maxOkCoordinates = 0;
                    for (int[] direction : directions) {
                        int nextRow = direction[0] + row;
                        int nextCol = direction[1] + col;
                        if (nextRow >= 0 && nextRow < 10 && nextCol >= 0 && nextCol < 8) {
                            maxOkCoordinates += 1;
                            if (mines[nextRow][nextCol] != 1) {
                                okCoordinates.add(new Pair<Integer, Integer>(nextRow, nextCol));
                            }
                        }
                    }
                    // There are mines nearby
                    if (okCoordinates.size() != maxOkCoordinates) {
                        // Hint the user how many
                        grids[row][col].setText(String.valueOf(maxOkCoordinates - okCoordinates.size()));
                    }
                    // All nearby grids are not mines
                    else {
                        // Add them to BFS queue
                        for (Pair<Integer, Integer> okCoordinate : okCoordinates) {
                            if (!visited.contains(okCoordinate)) {
                                queue.add(okCoordinate);
                                visited.add(okCoordinate);
                            }
                        }
                    }
                }
            }
        }
        else {
            int row = startCoordinate.first;
            int col = startCoordinate.second;
            if (((ColorDrawable) grids[row][col].getBackground()).getColor() == Color.GRAY) {
                return;
            }
            if (flags[row][col] == 1) {
                grids[row][col].setText("");
                flags[row][col] = 0;
            }
            else {
                grids[row][col].setText(R.string.flag);
                flags[row][col] = 1;
                // If the user wins
                if (isWin()) {
                    // Redirect to result page
                    Intent intent = new Intent(this, ResultActivity.class);
                    startActivity(intent);
                    return;
                }
            }
        }
    }

    public void changeMode(View view)
    {
        Button button = findViewById(R.id.mode);
        if (isPick) {
            isPick = false;
            button.setText(R.string.flag);
        }
        else {
            isPick = true;
            button.setText(R.string.pick);
        }
    }

    // Check if the user wins
    public boolean isWin() {
        return findMinesCoordinates().equals(findFlagsCoordinates());
    }

    public void showResult()
    {
        Intent intent = new Intent(this, ResultActivity.class);
        startActivity(intent);
    }
}