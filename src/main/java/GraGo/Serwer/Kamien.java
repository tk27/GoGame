package GraGo.Serwer;

import java.util.ArrayList;

public class Kamien {
    private int kolor; //1 - czarny, 2 - bialy
    private int x, y; //współrzędne kamienia

    public Kamien(int nowy_kolor, int x, int y){
        this.kolor = nowy_kolor;
        this.x = x;
        this.y = y;
    }

    /**
     * metoda czyOddech zwraca true jeśli pojedynczy kamień ma w swoim zasięgu puste pole (oddech) - nie bierze pod uwagę łańucha, do którego może należeć
     */
    public boolean czyOddech(int x, int y, Kamien[][] kamienie){
        if (x == 0 && y == 0)
            return kamienie[x + 1][y] == null || kamienie[x][y + 1] == null; // róg
        else if (x == 0 && y == 18)
            return kamienie[x + 1][y] == null || kamienie[x][y - 1] == null; // róg
        else if (x == 18 && y == 0)
            return kamienie[x - 1][y] == null || kamienie[x][y + 1] == null; // róg
        else if (x == 18 && y == 18)
            return kamienie[x - 1][y] == null || kamienie[x][y - 1] == null; // róg
        else if (x == 0 && y > 0 && y < 18)
            return kamienie[x + 1][y] == null || kamienie[x][y - 1] == null || kamienie[x][y + 1] == null; // bok
        else if (x == 18 && y > 0 && y < 18)
            return kamienie[x - 1][y] == null || kamienie[x][y - 1] == null || kamienie[x][y + 1] == null; // bok
        else if (x > 0 && x < 18 && y == 0)
            return kamienie[x + 1][y] == null || kamienie[x -1][y] == null || kamienie[x][y + 1] == null; // bok
        else if (x > 0 && x < 18 && y == 18)
            return kamienie[x + 1][y] == null || kamienie[x -1][y] == null || kamienie[x][y - 1] == null; // bok
        else
        return kamienie[x + 1][y] == null || kamienie[x - 1][y] == null || kamienie[x][y + 1] == null || kamienie[x][y - 1] == null; //środek
    }

    /**
     * metoda czyOddechLancuch() zwraca tablicę kamieni z usuniętym lancuchem kamieni z planszy jesli łańcuch, do którego należał dany kamień nie posiadał oddechów,
     * a w przypadku posiadania co najmniej jednego oddechu zwraca niezmienioną tablicę
     */
    public Kamien[][] czyOddechLancuch(int a, int b, Kamien[][] kamienie){
        ArrayList<Kamien> uduszoneKamienie = new ArrayList<>(); //arraylista z kamieniami, które zostały uduszone
        if (!uduszoneKamienie.contains(kamienie[a][b])){uduszoneKamienie.add(kamienie[a][b]);}
        if (a < 18) {
            if (kamienie[a + 1][b] != null) {
                if (!uduszoneKamienie.contains(kamienie[a + 1][b]) && kamienie[a + 1][b].kolor == this.kolor) {
                    if (czyOddech(a + 1, b, kamienie)){
                        System.out.println("a="+a+", b="+b+": RETURN NULL");
                        return null;
                    } else{
                        uduszoneKamienie.add(kamienie[a + 1][b]);
                    }
                    czyOddechLancuch(a + 1, b, kamienie);
                }
            } else {
                System.out.println("a="+a+", b="+b+": RETURN NULL");
                return null;
            }
        }

        if (a > 0) {
            if (kamienie[a - 1][b] != null) {
                if (!uduszoneKamienie.contains(kamienie[a - 1][b]) && kamienie[a - 1][b].kolor == this.kolor) {
                    if (czyOddech(a - 1, b, kamienie)) {
                        System.out.println("a="+a+", b="+b+": RETURN NULL");
                        return null;
                    }
                    else {
                        uduszoneKamienie.add(kamienie[a - 1][b]);
                        czyOddechLancuch(a - 1, b, kamienie);
                    }
                }
            } else {
                System.out.println("a="+a+", b="+b+": RETURN NULL");
                return null;
            }
        }

        if (b < 18) {
            if (kamienie[a][b + 1] != null) {
                if (!uduszoneKamienie.contains(kamienie[a][b + 1]) && kamienie[a][b + 1].kolor == this.kolor) {
                    if (czyOddech(a, b + 1, kamienie)) {
                        System.out.println("a="+a+", b="+b+": RETURN NULL");
                        return null;
                    }
                    else {
                        uduszoneKamienie.add(kamienie[a][b + 1]);
                        czyOddechLancuch(a, b + 1, kamienie);
                    }
                }
            } else {
                System.out.println("a="+a+", b="+b+": RETURN NULL");
                return null;
            }
        }

        if (b > 0) {
            if (kamienie[a][b - 1] != null) {
                if (!uduszoneKamienie.contains(kamienie[a][b - 1]) && kamienie[a][b - 1].kolor == this.kolor) {
                    if (czyOddech(a, b - 1, kamienie)) {
                        System.out.println("a="+a+", b="+b+": RETURN NULL");
                        return null;
                    }
                    else {
                        uduszoneKamienie.add(kamienie[a][b - 1]);
                        czyOddechLancuch(a, b - 1, kamienie);
                    }
                }
            } else {
                System.out.println("a="+a+", b="+b+": RETURN NULL");
                return null;
            }
        }



       /* for (int w = 0; w < 19; w++){
            for (int e=0; e < 19; e++){
                System.out.println("a="+a+", b="+b+":");
                System.out.print("kamienie["+w+"]["+e+"] = "+(kamienie[w][e] == null ? "null" : kamienie[w][e].kolor+", "));
            }
        }*/
        return kamienie;
}

    public int wezKolor(){
        return kolor;
    }

    public int wezX(){
        return x;
    }
    public int wezY(){
        return y;
    }

}

