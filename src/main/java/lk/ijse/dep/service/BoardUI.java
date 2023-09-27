package lk.ijse.dep.service;

public interface BoardUI {
    // implemented
    void update(int col, boolean isHuman);
    void notifyWinner(Winner winner);
}
