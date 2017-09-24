package com.bignerdranch.android.audiceive;

public class Score {
    private int sceneID;
   private int score;
    public Score(int sceneID, int score) {
        this.sceneID = sceneID;
        this.score = score;
        }

            public int getSceneID() {
              return sceneID;
            }

            public int getScore() {
               return score;
           }
}
