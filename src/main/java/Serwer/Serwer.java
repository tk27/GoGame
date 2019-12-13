package Serwer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Serwer {
    private Kamien[][] plansza_go = new Kamien[19][19];
    private Gracz aktualnyGracz;

    Serwer(){
        try (ServerSocket listener = new ServerSocket(58901)) {
            System.out.println("Serwer Go aktywny...");
            ExecutorService pool = Executors.newFixedThreadPool(100);
            while (true) {
                //socket = listener.accept();
                //Gracz gracz1 = new Gracz(listener.accept(), 2, this);
                //Gracz gracz2 = new Gracz(listener.accept(), 2, this);

                pool.execute(new Gracz(listener.accept(), 1, this)); //czarny
                pool.execute(new Gracz(listener.accept(), 2, this)); //bialy
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void polaczenieZGraczem(Gracz gracz) throws IOException {
        gracz.input = new Scanner(gracz.socket.getInputStream());
        gracz.output = new PrintWriter(gracz.socket.getOutputStream(), true);
        gracz.output.println("WITAJ " + gracz.kolor);
        if (gracz.kolor == 1) {
            gracz.output.println("INFO Oczekuje na przeciwnika");
            ustawAktualnegoGracza(gracz);
        } else {
            gracz.ustawPrzeciwnika(aktualnyGracz);
            aktualnyGracz.ustawPrzeciwnika(gracz);
            aktualnyGracz.output.println("INFO Twoja kolej");
            gracz.output.println("INFO Runda przeciwnika, proszę czekać");
        }
    }

    void interpretujKomendy(Gracz gracz) {
        while (gracz.input.hasNext()) {
            String polecenie = gracz.input.next();
            if (polecenie.startsWith("WYJSCIE")) {
                return;
            } else if (polecenie.startsWith("RUCH")) {
                try {
                    int x = Integer.parseInt(gracz.input.next());
                    int y = Integer.parseInt(gracz.input.next());
                    zweryfikujRuch(x, y, gracz);
                    czyUduszone(x, y);
                    gracz.output.println("POPRAWNY_RUCH " + x + " " + y);
                    gracz.przeciwnik.output.println("RUCH_PRZECIWNIKA " + x + " " + y);
                    gracz.pass = false;

                /*if () {
                    output.println("ZWYCIESTWO");
                    przeciwnik.output.println("PORAZKA");
                }*/
                } catch (IllegalStateException e) {
                    gracz.output.println("INFO " + e.getMessage());
                }
            } else if (polecenie.startsWith("PASS")) {
                gracz.output.println("SPASOWALES");
                gracz.przeciwnik.output.println("PRZECIWNIK_SPASOWAL");
                gracz.pass = true;
                ustawAktualnegoGracza(gracz.przeciwnik);
            }
            if(gracz.pass && gracz.przeciwnik.pass)
            {
                gracz.output.println("KONIEC_GRY");
                gracz.przeciwnik.output.println("KONIEC_GRY");
            }
        }
    }

    synchronized void zweryfikujRuch(int x, int y, Gracz gracz) {
        if (gracz != aktualnyGracz) {
            throw new IllegalStateException("Nie Twój ruch!");
        } else if (gracz.przeciwnik == null) {
            throw new IllegalStateException("Nie masz jeszcze przeciwnika");
        } else if (plansza_go[x][y] != null) {
            throw new IllegalStateException("Pole jest już zajęte!");
        } else if (czySamoboj(x, y)) {
            throw new IllegalStateException("Niedozwolony ruch samobojczy!");
        } else if (czyKO(x, y)) {
            throw new IllegalStateException("Niedozwolony ruch KO!");
        }
        plansza_go[x][y] = new Kamien(x, y, aktualnyGracz.kolor);
        ustawAktualnegoGracza(aktualnyGracz.przeciwnik);
    }

    void czyUduszone(int x, int y) {
        System.out.println("czyUduszone dla x: "+x+", y: "+y);
        if (plansza_go[x + 1][y] != null) {
            if (plansza_go[x + 1][y].kolor == aktualnyGracz.przeciwnik.kolor)
            plansza_go = plansza_go[x + 1][y].czyOddechLancuch(x + 1, y, plansza_go);
        }
        if (plansza_go[x - 1][y] != null) {
            if (plansza_go[x - 1][y].kolor == aktualnyGracz.przeciwnik.kolor)
            plansza_go = plansza_go[x - 1][y].czyOddechLancuch(x - 1, y, plansza_go);
        }

        if (plansza_go[x][y - 1] != null) {
            if (plansza_go[x][y - 1].kolor == aktualnyGracz.przeciwnik.kolor)
            plansza_go = plansza_go[x][y - 1].czyOddechLancuch(x, y - 1, plansza_go);
        }

        if (plansza_go[x][y + 1] != null) {
            if (plansza_go[x][y + 1].kolor == aktualnyGracz.przeciwnik.kolor)
            plansza_go = plansza_go[x][y + 1].czyOddechLancuch(x, y + 1, plansza_go);
        }
    }

    private boolean czyKO(int x, int y) {
        //TODO: funkcja sprawdzająca czy podany ruch nie będzie powtórzeniem
        return false;
    }

    private boolean czySamoboj(int x, int y) {
        plansza_go[x][y] = new Kamien(x, y, aktualnyGracz.kolor);
        if(plansza_go[x][y].czyOddechLancuch(x, y, plansza_go) == null){
            plansza_go[x][y] = null;
            return false;
        } else{
            plansza_go[x][y] = null;
            return true;
        }

    }

    public void ustawAktualnegoGracza(Gracz gracz){
        this.aktualnyGracz = gracz;
        System.out.println("Aktualny gracz: "+gracz.kolor);
    }

    void wyjscieZGry(Gracz gracz){
        if (gracz.przeciwnik != null && gracz.przeciwnik.output != null) {
            gracz.przeciwnik.output.println("PRZECIWNIK_WYSZEDL");
        }
        try {gracz.socket.close();} catch (IOException ignored) {}
    }
}
