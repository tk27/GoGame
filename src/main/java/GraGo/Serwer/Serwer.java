package GraGo.Serwer;

import GraGo.KomunikatyKlienta;
import GraGo.KomunikatySerwera;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Serwer {
    private Kamien[][] planszaGo = new Kamien[19][19];
    private Gracz aktualnyGracz;
    private Boolean czyBot = false;
    private Bot bot;

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
        gracz.output.println(KomunikatySerwera.WITAJ+ " " + gracz.wezKolor());
        String przeciwnik = gracz.input.next();
        System.out.println("Typ przeciwnika: "+przeciwnik);
        if (przeciwnik.equals("Bot")){
            czyBot = true;
            bot = new Bot(gracz.wezKolor() == 1 ? 2 : 1);
            gracz.output.println(KomunikatySerwera.INFO + " Grasz z botem");
            ustawAktualnegoGracza(gracz);
        } else {
            if (gracz.wezKolor() == 1) {
                gracz.output.println(KomunikatySerwera.INFO + " Oczekuje na przeciwnika");
                ustawAktualnegoGracza(gracz);
            } else {
                gracz.ustawPrzeciwnika(aktualnyGracz);
                aktualnyGracz.ustawPrzeciwnika(gracz);
                aktualnyGracz.output.println(KomunikatySerwera.INFO + " Twoja kolej");
                gracz.output.println(KomunikatySerwera.INFO + " Runda przeciwnika, proszę czekać");
            }
        }
    }

    void interpretujKomendy(Gracz gracz) throws InterruptedException {
        while (gracz.input.hasNext()) {
            String polecenie = gracz.input.next();
            if (polecenie.startsWith(KomunikatyKlienta.WYJSCIE.toString())) {
                return;
            } else if (polecenie.startsWith(KomunikatyKlienta.RUCH.toString())) {
                try {
                    int x = Integer.parseInt(gracz.input.next());
                    int y = Integer.parseInt(gracz.input.next());
                    zweryfikujRuch(x, y, gracz);
                    gracz.output.println(KomunikatySerwera.POPRAWNY_RUCH + " " + x + " " + y);
                    //sprawdzUduszone(x, y, gracz);
                    if (czyBot){
                        Thread.sleep(200);
                        String parametry = bot.wykonajRuch(planszaGo);
                        int botX = Character.digit(parametry.charAt(0), 10);
                        int botY = Character.digit(parametry.charAt(2), 10);
                        gracz.output.println(KomunikatySerwera.RUCH_PRZECIWNIKA + " " + botX + " " + botY);
                        planszaGo[botX][botY] = new Kamien(aktualnyGracz.wezKolor(), botX, botY);
                        //sprawdzUduszone(botX, botY, gracz);
                    } else{
                        ustawAktualnegoGracza(aktualnyGracz.przeciwnik);
                        gracz.przeciwnik.output.println(KomunikatySerwera.RUCH_PRZECIWNIKA + " " + x + " " + y);
                    }
                    gracz.pass = false;
                } catch (IllegalStateException | InterruptedException e) {
                    gracz.output.println(KomunikatySerwera.INFO + " " + e.getMessage());
                }
            }else if (polecenie.startsWith(KomunikatySerwera.ZWYCIESTWO.toString())) {
                gracz.output.println(KomunikatySerwera.ZWYCIESTWO.toString());
                gracz.przeciwnik.output.println(KomunikatySerwera.PORAZKA);
            }else if (polecenie.startsWith(KomunikatySerwera.PORAZKA.toString())) {
                gracz.output.println(KomunikatySerwera.PORAZKA);
                gracz.przeciwnik.output.println(KomunikatySerwera.ZWYCIESTWO);
            }else if (polecenie.startsWith(KomunikatySerwera.REMIS.toString())) {
                gracz.output.println(KomunikatySerwera.REMIS);
                gracz.przeciwnik.output.println(KomunikatySerwera.REMIS);
            }else if (polecenie.startsWith(KomunikatySerwera.PASS.toString())) {
                gracz.pass = true;
                if (!czyBot){
                    gracz.przeciwnik.output.println(KomunikatySerwera.PASS);
                    ustawAktualnegoGracza(gracz.przeciwnik);
                }
            }else if (polecenie.startsWith(KomunikatySerwera.WYNIK.toString())) {
                int a = Integer.parseInt(gracz.input.next());
                int b = Integer.parseInt(gracz.input.next());
                gracz.output.println(KomunikatySerwera.WYNIK + " " + a + " " + b);
            }
            if(czyBot && gracz.pass){
                gracz.output.println(KomunikatySerwera.KONIEC_GRY);
            } else if (gracz.pass && gracz.przeciwnik.pass) {
                gracz.output.println(KomunikatySerwera.KONIEC_GRY);
                gracz.przeciwnik.output.println(KomunikatySerwera.KONIEC_GRY);
            }
        }
    }

    private synchronized void zweryfikujRuch(int x, int y, Gracz gracz) {
        if (!czyBot && gracz != aktualnyGracz) {
            throw new IllegalStateException("Nie Twój ruch!");
        } else if (!czyBot && gracz.przeciwnik == null) {
            throw new IllegalStateException("Nie masz jeszcze przeciwnika");
        } else if (planszaGo[x][y] != null) {
            throw new IllegalStateException("Pole jest już zajęte!");
        } else if (czySamoboj(x, y)) {
            throw new IllegalStateException("Niedozwolony ruch samobojczy!");
        } else if (czyKO(x, y)) {
            throw new IllegalStateException("Niedozwolony ruch KO!");
        }
        planszaGo[x][y] = new Kamien(aktualnyGracz.wezKolor(), x, y);
    }

    private boolean czyKO(int x, int y) {
        //TODO: funkcja sprawdzająca czy podany ruch nie będzie powtórzeniem
        return false;
    }

    private boolean czySamoboj(int x, int y) {
        planszaGo[x][y] = new Kamien(aktualnyGracz.wezKolor(), x, y);
        if(planszaGo[x][y].czyOddechLancuch(x, y, planszaGo) == null){
            planszaGo[x][y] = null;
            return false;
        } else{
            planszaGo[x][y] = null;
            return true;
        }

    }

    /*private void sprawdzUduszone(int x, int y, Gracz gracz){
        if (planszaGo[x+1][y] != null && planszaGo[x+1][y].wezKolor() == gracz.przeciwnik.wezKolor()){
            if (planszaGo[x+1][y].czyOddechLancuch(x+1, y, planszaGo) != null){
                ArrayList<Kamien> uduszoneKamienie = planszaGo[x+1][y].czyOddechLancuch(x+1, y, planszaGo);
                wyslijUduszoneKamienie(uduszoneKamienie, gracz);
            }
        }
        if (planszaGo[x-1][y] != null && planszaGo[x-1][y].wezKolor() == gracz.przeciwnik.wezKolor()){
            if (planszaGo[x-1][y].czyOddechLancuch(x-1, y, planszaGo) != null){
                ArrayList<Kamien> uduszoneKamienie = planszaGo[x-1][y].czyOddechLancuch(x-1, y, planszaGo);
                wyslijUduszoneKamienie(uduszoneKamienie, gracz);
            }
        }
        if (planszaGo[x][y+1] != null && planszaGo[x][y+1].wezKolor() == gracz.przeciwnik.wezKolor()){
            if (planszaGo[x][y+1].czyOddechLancuch(x, y+1, planszaGo) != null){
                ArrayList<Kamien> uduszoneKamienie = planszaGo[x][y+1].czyOddechLancuch(x, y+1, planszaGo);
                wyslijUduszoneKamienie(uduszoneKamienie, gracz);
            }
        }
        if (planszaGo[x][y-1] != null && planszaGo[x][y-1].wezKolor() == gracz.przeciwnik.wezKolor()){
            if (planszaGo[x][y-1].czyOddechLancuch(x, y-1, planszaGo) != null){
                ArrayList<Kamien> uduszoneKamienie = planszaGo[x][y-1].czyOddechLancuch(x, y-1, planszaGo);
                wyslijUduszoneKamienie(uduszoneKamienie, gracz);
            }
        }
    }

    private void wyslijUduszoneKamienie(ArrayList<Kamien> uduszoneKamienie, Gracz gracz){
        for (Kamien uduszonyKamien : uduszoneKamienie){
            gracz.output.println(KomunikatySerwera.USUN + " " + uduszonyKamien.wezX() + " " + uduszonyKamien.wezY());
            gracz.przeciwnik.output.println(KomunikatySerwera.USUN + " " + uduszonyKamien.wezX() + " " + uduszonyKamien.wezY());
        }
    }*/

    private void ustawAktualnegoGracza(Gracz gracz){
        this.aktualnyGracz = gracz;
        System.out.println("Aktualny gracz: "+gracz.wezKolor());
    }

    void wyjscieZGry(Gracz gracz){
        if (gracz.przeciwnik != null && gracz.przeciwnik.output != null) {
            gracz.przeciwnik.output.println(KomunikatySerwera.PRZECIWNIK_WYSZEDL);
        }
        try {gracz.socket.close();} catch (IOException ignored) {}
    }
}

