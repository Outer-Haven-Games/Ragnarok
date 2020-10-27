package outerhaven;

import java.util.ArrayList;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import outerhaven.Personnages.Personne;

public class Case {
    private int id;
    private double posX;
    private double posY;
    private Color couleur;
    private ArrayList<Case> caseVoisines;
    private ArrayList<Personne> contenu;

    public static Image hexagone_img1 = new Image(
            "https://cdn.discordapp.com/attachments/764528562429624391/764556130671132672/hexagon.png");
    public static Image hexagone_img2 = new Image(
            "https://cdn.discordapp.com/attachments/764528562429624391/764556132613488680/hexagon2.png");

    public Case(int id) {
        this.id = id;
        contenu = new ArrayList<Personne>();
        caseVoisines = new ArrayList<Case>();
        // this.posX = posX;
        // this.posY = posY;
    }

    public ImageView afficherCase(double X, double Y, double taille) {
        if (!estOccupe()) {
            // Image hexagone_img1 = new
            // Image(Case.class.getResourceAsStream("Images/hexagon.png"));
            ImageView hexagone = new ImageView(hexagone_img1);
            hexagone.setFitHeight(taille);
            hexagone.setFitWidth(taille);
            hexagone.setX(X);
            hexagone.setY(Y + 2000);
            this.posX = X;
            this.posY = Y + 20;
            hexagone.setOnMouseEntered((mouseEvent) -> {
                hexagone.setImage(hexagone_img2);
            });
            hexagone.setOnMouseExited((mouseEvent) -> {
                hexagone.setImage(hexagone_img1);
            });
            // hexagone.setEffect();
            arriveCase(hexagone);
            return hexagone;
        } else {
            return null;
        }
    }

    private void arriveCase(ImageView image) {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(5), ev -> {
            mouvementY(5, image);
        }));
        timeline.setCycleCount(400);
        timeline.play();
    }

    private void mouvementY(int pixel, ImageView image) {
        image.setY(image.getY() - pixel);
    }

    public int getId() {
        return id;
    }

    public boolean estOccupe() {
        if (contenu.size() == 0) {
            return false;
        } else {
            if (contenu.size() == 1) {
                return true;
            }
            System.out.println("Attention la case contient plus d'un obstacle/unite");
            return true;
        }
    }

    public double getPosX() {
        return posX;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public double getPosY() {
        return posY;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public ArrayList<Case> getCaseVoisines() {
        return caseVoisines;
    }

    public void setCaseVoisines(ArrayList<Case> voisines) {
        this.caseVoisines = voisines;
    }

    public void addVoisin(Case v) {
        caseVoisines.add(v);
    }

    public void setContenu(Personne p) {
        if (estOccupe()) {
            System.out.println("la case contient deja un obstacle/unite");
            contenu.add(p);
        }
    }

    public ArrayList<Personne> getContenu() {
        return contenu;
    }

    public void setCouleur(Color couleur) {
        this.couleur = couleur;
    }

    public ArrayList<Case> voisinsLibres(boolean lib) {
        ArrayList<Case> libres = new ArrayList<>();
        for (Case case1 : caseVoisines) {
            if (lib) {
                if (!case1.estOccupe()) {
                    libres.add(case1);
                }
            } else {
                if (case1.estOccupe()) {
                    libres.add(case1);
                }
            }

        }
        return libres;
    }

    @Deprecated
    public ArrayList<Case> pathToPerso(Personne p, ArrayList<Case> parcours) {
        parcours.add(this);
        if (contenu.contains(p)) {
            return parcours;
        }
        ArrayList<Case> aParcourir = new ArrayList<Case>();
        for (Case case1 : voisinsLibres(true)) {
            if (!parcours.contains(case1)) {
                aParcourir.add(case1);
            }
        }
        ArrayList<ArrayList<Case>> cheminsEnfants = new ArrayList<ArrayList<Case>>();
        for (Case case1 : aParcourir) {
            cheminsEnfants.add(case1.pathToPerso(p, parcours));
        }
        ArrayList<Case> shorterPath = new ArrayList<Case>();
        for (ArrayList<Case> path : cheminsEnfants) {
            if (shorterPath.size() == 0) {
                shorterPath = path;
            } else {
                if (path.size() < shorterPath.size()) {
                    shorterPath = path;
                }
            }
        }
        return shorterPath;
    }

    public ArrayList<Case> pathToPersoAux(Equipe equipe, ArrayList<Case> parcours, int depth) {
        if (depth == 0) {
            if (contenu.get(0).getTeam() == equipe) {
                parcours.add(this);
                return parcours;
            }
        }
        ArrayList<ArrayList<Case>> parcoursEnfants = new ArrayList<ArrayList<Case>>();
        parcours.add(this);
        for (Case voisin : voisinsLibres(depth != 1)) {
            ArrayList<Case> parcoursVoisin = voisin.pathToPersoAux(equipe, parcours, -1);
            if (parcoursVoisin.size() == depth) {
                parcoursEnfants.add(parcoursVoisin);
            }
        }
        return parcoursEnfants.get(0);
    }

    // necessite que le plateau contienne au moins un personnage de l'equipe visee
    // en dehors de this
    public ArrayList<Case> pathToPerso(Equipe e) {
        ArrayList<Case> parcours = new ArrayList<Case>();
        int depth = 1;
        while (parcours.isEmpty()) {
            parcours = pathToPersoAux(e, new ArrayList<Case>(), depth);
            depth++;
        }
        return parcours;
    }

}
