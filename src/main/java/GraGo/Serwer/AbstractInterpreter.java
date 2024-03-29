package GraGo.Serwer;

import java.util.ArrayList;

abstract class AbstractInterpreter {
    public Kamien[][] planszaGo = new Kamien[19][19];
    ArrayList<Kamien> uduszoneKamienie = new ArrayList<>();
    Kamien ostatniUsunietyKamien; //zmienna dla metody czyKO

    abstract void zweryfikujRuch(int x, int y, Gracz gracz, AbstractGracz aktualnyGracz, boolean czyBot);
    abstract boolean czyKO(Kamien kamien);
    abstract boolean czySamoboj(Kamien kamien);

    abstract ArrayList<Kamien> czyOddech(Kamien kamien);
    abstract boolean czySasiedniePoleWolne(Kamien kamien);
    abstract void sprawdzUduszone(Kamien kamien, AbstractGracz gracz, Gra serwer);
}









