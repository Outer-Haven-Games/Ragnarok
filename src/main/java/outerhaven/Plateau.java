package outerhaven;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import outerhaven.Entites.Personnages.PersonnagesPrime.*;
import outerhaven.Entites.Personnages.Personne;
import outerhaven.Interface.BarrePersonnage;
import outerhaven.Interface.Bouton;
import outerhaven.Interface.Effets;
import outerhaven.Mecaniques.Alterations.Alteration;
import outerhaven.Mecaniques.Enchere;
import outerhaven.Mecaniques.Evenements.Evenement;
import outerhaven.Mecaniques.Sauvegarde;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * La classe Plateau va principalement générer le plateau et son interface, ainsi que les paramètres nécessaire au fonctionnement du jeu et aux interactions.
 */

public class Plateau {
    public static Group group = new Group();
    public static Scene scene = new Scene(group);
    public static double taille;
    /**
     * Informations sur une partie en cours.
     */
    public static int argentPartie = 0;
    public static int temps = 500;
    public static int tour = 0;
    /**
     * Status de certains paramètre utiles pour le fonctionnement du jeu.
     */
    public static Personne personneSelectionne;
    public static Equipe equipeSelectionne;
    public static boolean statusPartie = false;
    public static boolean activerAnimation = false;
    public static boolean activerEnchere = false;
    public static boolean enchereTerminee = false;
    public static boolean activerDijkstra = false;
    public static boolean activerCheats = false;
    public static boolean activerArgent = false;
    public static AtomicInteger idEnchere = new AtomicInteger();
    public static Text prix = new Text();
    /**
     * Interface utile au plateau à mettre à jour durant une partie.
     */
    public static BarrePersonnage barre = new BarrePersonnage();
    /**
     * Information javaFX utile pour le fonctionnement du jeu.
     */
    private static Stage primary;
    private static Group nbPersonne = new Group();
    private static Group nbTour = new Group();
    public double largeurMax = Screen.getPrimary().getVisualBounds().getHeight();
    public double longueurMax = Screen.getPrimary().getVisualBounds().getWidth();
    /**
     * Aire du plateau et taille en pixels d'une case.
     */
    private int aire;
    private Text infoArgent;
    private TextField nbArgent;

    public Plateau(Stage primary) {
        // Le constructeur n'as besoin que de la fenêtre du main pour se lancer
        Plateau.primary = primary;
    }

    /**
     * Méthode permettant d'afficher le nombre de personnes contenus dans chaque équipe.
     */
    public static void afficherNbPersonne() {
        Rectangle barre = new Rectangle(250, 70, Color.LIGHTGRAY);
        barre.setX(Screen.getPrimary().getVisualBounds().getWidth() - 260);
        barre.setY(10);
        barre.setStroke(Color.BLACK);
        barre.setStrokeWidth(2);

        Text title = new Text("Nombre de personnes par équipe");
        title.setX(barre.getX() + 10);
        title.setY(barre.getY() + 20);

        Text equipes = new Text("Equipe 1 : " + getE1().getNbPersonne() + "\n" + "Equipe 2 : " + getE2().getNbPersonne());
        equipes.setX(title.getX());
        equipes.setY(title.getY() + 20);

        nbPersonne.getChildren().add(barre);
        nbPersonne.getChildren().add(title);
        nbPersonne.getChildren().add(equipes);
    }

    /**
     * Méthode pour update les compteurs de afficherNbPersonne().
     */
    public static void updateNbPersonne() {
        nbPersonne.getChildren().clear();
        afficherNbPersonne();
    }

    /**
     * Affiche le tour actuel.
     */
    private static void afficheNbTour() {
        Rectangle barre = new Rectangle(50, 50, Color.LIGHTGRAY);
        barre.setX(Screen.getPrimary().getVisualBounds().getWidth() - 260);
        barre.setY(90);
        barre.setStroke(Color.BLACK);
        barre.setStrokeWidth(2);

        Text nb = new Text("Tour\n" + tour);
        nb.setX(barre.getX() + 10);
        nb.setY(barre.getY() + 20);

        nbTour.getChildren().add(barre);
        nbTour.getChildren().add(nb);
    }

    /**
     * Méthode pour update les compteurs de afficheNbTour().
     */
    public static void updateNbTour() {
        nbTour.getChildren().clear();
        afficheNbTour();
    }

    /**
     * Fonction transformant un String en entier.
     *
     * @param textField est le texte qu'on cherche à transformer en entier
     * @return le texte en int s'il contient que des entiers
     */
    public static int getIntFromTextField(TextField textField) {
        String text = textField.getText();
        return Integer.parseInt(text);
    }

    /**
     * equipeSelectionne devient l'équipe en paramètre.
     */
    public static void incorporeEquipe(Equipe equipe) {
        equipeSelectionne = equipe;
    }

    /**
     * Affiche ou supprime le brouillard de guerre quand cette méthode est appelée.
     */
    public static void brouillard() {
        if (equipeSelectionne == getE1()) {
            // Tests brouillard de guerre
            for (int i = 0; i < Case.listeCase.size(); i++) {
                if (i >= Case.listeCase.size() / 2) {
                    if (Case.listeCase.get(i).getContenu().isEmpty() || Case.listeCase.get(i).getContenu().get(0).getTeam() != Equipe.e1) {
                        Case.listeCase.get(i).devenirNoir();
                    } else {
                        Case.listeCase.get(i).devenirBlanc();
                    }
                } else {
                    Case.listeCase.get(i).devenirBlanc();
                }
            }
        } else {
            for (int i = 0; i < Case.listeCase.size(); i++) {
                if (i < Case.listeCase.size() / 2) {
                    if (Case.listeCase.get(i).getContenu().isEmpty() || Case.listeCase.get(i).getContenu().get(0).getTeam() != Equipe.e2) {
                        Case.listeCase.get(i).devenirNoir();
                    } else {
                        Case.listeCase.get(i).devenirBlanc();
                    }
                } else {
                    Case.listeCase.get(i).devenirBlanc();
                }
            }
        }
    }

    /**
     * Cette section contient tout les getters et setters de Plateau
     */

    public static boolean isActiverAnimation() {
        return activerAnimation;
    }

    public static void setStatusPartie(boolean statusPartie) {
        Plateau.statusPartie = statusPartie;
    }

    public static Equipe getE1() {
        return Equipe.e1;
    }

    public static Equipe getE2() {
        return Equipe.e2;
    }

    public void lancerPartie() {
        // Lance une partie en ajoutant la scene et le groupe dans la fenêtre principale
        interfaceDebut();
        primary.setScene(scene);
        primary.show();
    }

    /**
     * Interface avant la generation du plateau
     * on ajoute ici les boutons :
     * Start (pour lancer la partie)
     * Quitter (pour arrêter le jeu)
     * Une entrée pour l'aire du plateau
     * Une entrée pour l'argent des équipes
     * (avec tout les texte qui vont avec)
     */
    private void interfaceDebut() {
        Button start = new Button("START");
        start.setLayoutX((longueurMax - 700) / 2);
        start.setLayoutY((largeurMax - 200) / 2);
        start.setMinSize(700, 200);
        start.setStyle("-fx-background-color: lightgrey;-fx-border-style: solid;-fx-border-width: 2px;-fx-border-color: black;-fx-font-weight: bold;-fx-font-size: 60");
        start.setOnMouseEntered(mouseEvent -> start.setEffect(new Effets().putInnerShadow(Color.ORANGE)));
        start.setOnMouseExited(mouseEvent -> start.setEffect(null));

        // Ajout de textes
        Text infoNB = new Text("Entrez le nombre de cases du plateau :");
        infoNB.setLayoutX((longueurMax - 700) / 2);
        infoNB.setLayoutY((largeurMax - 300) / 2 - 20);

        infoArgent = new Text("Entrez la limite d'argent pour chaque équipe : (vide = pas de limite)");
        infoArgent.setLayoutX((longueurMax - 700) / 2);
        infoArgent.setLayoutY((largeurMax - 450) / 2 - 20);

        // Entrée de l'aire du plateau
        TextField nbCase = new TextField();
        nbCase.setLayoutX((longueurMax - 700) / 2);
        nbCase.setLayoutY((largeurMax - 280) / 2 - 20);
        nbCase.setMinSize(100, 50);
        nbCase.setStyle("-fx-background-color: lightgrey;-fx-border-style: solid;-fx-border-width: 2px;-fx-border-color: black");

        // Entrée de l'argent
        nbArgent = new TextField();
        nbArgent.setLayoutX((longueurMax - 700) / 2);
        nbArgent.setLayoutY((largeurMax - 430) / 2 - 20);
        nbArgent.setMinSize(100, 50);
        nbArgent.setStyle("-fx-background-color: lightgrey;-fx-border-style: solid;-fx-border-width: 2px;-fx-border-color: black");

        nbCase.setOnKeyReleased(key -> {
            if (key.getCode() == KeyCode.ENTER) {
                aire = getIntFromTextField(nbCase);
                if (nbArgent.getText().length() > 0) {
                    argentPartie = getIntFromTextField(nbArgent);
                }
                if (aire > 0) {
                    group.getChildren().clear();
                    sceneSuivante();
                }
            }
        });

        nbArgent.setOnKeyReleased(key -> {
            if (key.getCode() == KeyCode.ENTER) {
                aire = getIntFromTextField(nbCase);
                if (nbArgent.getText().length() > 0) {
                    argentPartie = getIntFromTextField(nbArgent);
                }
                if (aire > 0) {
                    group.getChildren().clear();
                    sceneSuivante();
                }
            }
        });

        // Configuration du bouton start
        start.setOnMouseClicked(mouseEvent -> {
            aire = getIntFromTextField(nbCase);
            if (nbArgent.getText().length() > 0) {
                argentPartie = getIntFromTextField(nbArgent);
            }
            if (aire > 0) {
                group.getChildren().clear();
                sceneSuivante();
            }
        });

        ajouteLesModes();
        ajouteLesOptions();
        Button quitter = boutonExit();
        quitter.setLayoutY(10);

        // Ajout de toute ces interfaces dans le group
        group.getChildren().addAll(infoNB, nbCase, start, quitter);
    }

    /**
     * lancerScenePlateau() génère le plateau d'hexagone en fonction de :
     * - l'aire
     * - la taille de l'écran de l'utilisateur
     * La methode auras aussi pour but d'incorporer les Cases créent dans listeCase et tableauCase en leur donnant des coordonnés x et y.
     * Elle ajoute aussi des interface en plus comme le menus, ses boutons d'interactions, l'argent du joueur et le nb de personnage dans chaque équipes.
     */
    public void lancerScenePlateau() {
        // Ajuste la taille d'une case et le taille du tableau case
        taille = 1000 / Math.sqrt(aire);
        Case.tableauCase = new Case[(int) Math.sqrt(aire) + 1][(int) Math.sqrt(aire) + 2];

        // Paramétrage des événements aléatoires
        Evenement.setFréquenceEvenement(15);
        Evenement.setPourcentageEvenement(10);

        // Génération du plateau
        genererPlateau();
        // Initialisation des cases voisines
        initVoisins();
        // Creation et incorporation d'une slide barre + bouton
        ajouteLeMenu();
        // Creation et incorporation d'information sur les équipes
        afficherNbPersonne();
        afficheNbTour();
        // On affiche l'argent si elle n'est pas infini (rien d'écrit)
        if (argentPartie > 0) {
            if (!activerEnchere) {
                getE1().setArgent(argentPartie);
                getE2().setArgent(argentPartie);
            }
            group.getChildren().add(barre.getArgentGroup());
        }
        personneSelectionne = null;
        if (enchereTerminee) {
            equipeSelectionne = Equipe.e1;
            barre.majBarreEnchere();
            brouillard();
        }
        if (activerCheats) {
            cheats();
        }
        // On ajoute toutes les interfaces
        group.getChildren().addAll(nbPersonne, nbTour, boutonPausePlay(), barre.returnBarre());
        primary.setScene(scene);
    }

    /**
     * Cette methode a pour but de générer un plateau de case en fonction du l'aire voulu par l'utilisateur.
     * Elle n'interagit qu'avec des attributs de la classe plateau.
     */
    private void genererPlateau() {
        // Les hexagones se chevauchent par ligne, le but de se boolean est de décaler chaque ligne pour permettre ce chevauchement
        boolean decalage = false;
        int i = 0;
        int ligne = 0;

        while (i < aire) {
            // On entre dans une ligne
            if (!decalage) {
                double posY = largeurMax / 2 - (taille * Math.sqrt(aire) / 2) + ligne * taille - taille * 1.05 * ligne / 4;
                for (int j = 0; j < Math.sqrt(aire); j++) {
                    // On définie les cases d'une ligne
                    double posX = longueurMax / 2 - (taille * (Math.sqrt(aire)) / 2) + j * taille * 0.99;
                    Case hexagone = new Case(ligne, j - (ligne / 2));
                    // Ajout de la case dans une liste, tableau et groupe (pour qu'elle s'affiche)
                    Case.tableauCase[ligne][j] = hexagone;
                    Case.listeCase.add(hexagone);
                    group.getChildren().add(hexagone.afficherCase(posX, posY, taille));
                    i++;
                }
                decalage = true;
                ligne++;
            } else {
                double posY = largeurMax / 2 - (taille * Math.sqrt(aire) / 2) + ligne * taille - taille * 1.05 * ligne / 4;
                for (int j = 0; j < Math.sqrt(aire); j++) {
                    // On définie les cases d'une ligne
                    double posX = longueurMax / 2 - (taille * (Math.sqrt(aire)) / 2) + j * taille * 0.99 - taille / 2;
                    Case hexagone = new Case(ligne, j - ((ligne) / 2 + 1));
                    // Ajout de la case dans une liste, tableau et groupe (pour qu'elle s'affiche)
                    Case.tableauCase[ligne][j] = hexagone;
                    Case.listeCase.add(hexagone);
                    group.getChildren().add(hexagone.afficherCase(posX, posY, taille));
                    i++;
                }
                decalage = false;
                ligne++;
            }
        }
    }

    /**
     * Permet de lancer la scene des enchères si le mode enchère est activé,
     * il gere le système de mise et personnage prime misé.
     */
    public void lancerSceneEnchere() {
        Button terminerEnchere = new Bouton().creerBouton("Terminer");
        terminerEnchere.setLayoutX(140);
        terminerEnchere.setLayoutY(10);

        if (argentPartie > 0) {
            getE1().setArgent(argentPartie);
            getE2().setArgent(argentPartie);
            group.getChildren().add(barre.getArgentGroup());
        }

        terminerEnchere.setOnMouseClicked(mouseEvent -> {
            group.getChildren().clear();
            enchereTerminee = true;
            BarrePersonnage.setSave(new Sauvegarde());
            sceneSuivante();
        });

        Rectangle cadre = new Rectangle(225, 48, Color.LIGHTGRAY);
        cadre.setStroke(Color.BLACK);
        cadre.setStrokeWidth(2);
        cadre.setX(700);
        cadre.setY(290);

        // Tests enchères
        Enchere.ajouterEnchere(new Enchere(new PaladinPrime()));
        Enchere.ajouterEnchere(new Enchere(new NecromancienPrime()));
        Enchere.ajouterEnchere(new Enchere(new AlchimistePrime()));
        Enchere.ajouterEnchere(new Enchere(new PretrePrime()));
        Enchere.ajouterEnchere(new Enchere(new ArchimagePrime()));
        Collections.shuffle(Enchere.getListeEnchere());
        Personne.personnages.clear();

        Group infosEnchere = new Group();
        infosEnchere.getChildren().addAll(Enchere.getListeEnchere().get(idEnchere.get()).afficherInformations(), Enchere.getListeEnchere().get(idEnchere.get()).getProduit().getImagePersonPosition(1200, 375));

        prix.setText(Enchere.getListeEnchere().get(idEnchere.get()).getPrixMinimal() + " €");
        prix.setX(cadre.getX() + 10);
        prix.setY(cadre.getY() + 35);
        prix.setStyle("-fx-font-weight: bold;-fx-font-size: 30");

        TextField encherirField = new TextField();
        encherirField.setLayoutX(699);
        encherirField.setLayoutY(567);
        encherirField.setMinSize(100, 50);
        encherirField.setStyle("-fx-background-color: lightgrey;-fx-border-style: solid;-fx-border-width: 2px;-fx-border-color: black");
        encherirField.setOnKeyReleased(key -> {
            if (key.getCode() == KeyCode.ENTER) {
                if (equipeSelectionne != null && getIntFromTextField(encherirField) <= equipeSelectionne.getArgent()) {
                    Plateau.equipeSelectionne.augmenterEnchere(getIntFromTextField(encherirField), Enchere.getListeEnchere().get(idEnchere.get()));
                    prix.setText(Enchere.getListeEnchere().get(idEnchere.get()).getPrixMinimal() + " €");
                    prix.setEffect(new Effets().putInnerShadow(Enchere.getListeEnchere().get(idEnchere.get()).getEquipeGagnante().getCouleur()));
                }
            }
        });

        Button boutonSeCoucher = new Bouton().creerBouton("Se coucher");
        boutonSeCoucher.setLayoutX(terminerEnchere.getLayoutX() + 130);
        boutonSeCoucher.setLayoutY(terminerEnchere.getLayoutY());

        group.getChildren().addAll(boutonSeCoucher, cadre, prix);

        boutonSeCoucher.setOnMouseClicked(mouseEvent -> {
            Enchere.getListeEnchere().get(idEnchere.get()).cloreEnchere();
            if (idEnchere.get() < 2) {
                group.getChildren().removeAll(boutonSeCoucher, cadre, prix);
                infosEnchere.getChildren().clear();
                idEnchere.getAndIncrement();
                infosEnchere.getChildren().addAll(Enchere.getListeEnchere().get(idEnchere.get()).afficherInformations(), Enchere.getListeEnchere().get(idEnchere.get()).getProduit().getImagePersonPosition(1200, 375));
                Enchere.getListeEnchere().get(idEnchere.get()).afficherInformations();
                prix.setText(0 + " €");
                encherirField.setText("");
                prix.setEffect(null);
                group.getChildren().addAll(boutonSeCoucher, cadre, prix);
                barre.equipe1.setEffect(null);
                barre.equipe2.setEffect(null);
                equipeSelectionne = null;
            } else {
                group.getChildren().clear();
                enchereTerminee = true;
                BarrePersonnage.setSave(new Sauvegarde());
                sceneSuivante();
            }
        });

        // Creation et incorporation d'une slide barre + bouton
        ajouteLeMenu();
        group.getChildren().addAll(terminerEnchere, encherirField, infosEnchere, barre.boutonEquipe()/*, prix*/);
        primary.setScene(scene);
    }

    /**
     * méthode permettant de poursuivre les enchères après que quelqu'un se couche.
     */
    public void sceneSuivante() {
        if (activerEnchere && !enchereTerminee) {
            if (argentPartie == 0) {
                argentPartie = 10000;
            }
            lancerSceneEnchere();
        } else {
            barre.interfaceBarre();
            lancerScenePlateau();
        }
    }

    /**
     * Méthode permettant à chaque case de connaitre ses cases voisines.
     */
    public void initVoisins() {
        for (Case c : Case.listeCase) {
            c.trouverVoisin();
        }
    }

    /**
     * Cette section contiendras les boutons du Plateau, on retrouveras le système de tour plus tard.
     */

    private Button boutonAnimation() {
        // Demande l'utilisation des animations
        Button animationBT = new Button("Animations : NON");
        animationBT.setMinSize(120, 50);
        animationBT.setLayoutX(140);
        animationBT.setLayoutY(70);
        animationBT.setStyle("-fx-background-color: lightgrey;-fx-border-style: solid;-fx-border-width: 2px;-fx-border-color: black");
        animationBT.setOnMouseClicked(mouseEvent -> {
            if (!activerAnimation) {
                activerAnimation = true;
                animationBT.setEffect(new Effets().putInnerShadow(Color.BLACK));
                animationBT.setText("Animations : OUI");
            } else {
                activerAnimation = false;
                animationBT.setEffect(null);
                animationBT.setText("Animations : NON");
            }
        });
        animationBT.setOnMouseEntered(mouseEvent -> {
            if (!activerAnimation) {
                animationBT.setEffect(new Effets().putInnerShadow(Color.BLACK));
            }
        });
        animationBT.setOnMouseExited(mouseEvent -> {
            if (!activerAnimation) {
                animationBT.setEffect(null);
            }
        });
        return animationBT;
    }

    /**
     * Génère le bouton pour activer les enchères.
     *
     * @return button affichable
     */

    private Button boutonEnchere() {
        // Demande l'utilisation des enchères
        Button enchereBT = new Button("Enchères : NON");
        enchereBT.setMinSize(120, 50);
        enchereBT.setLayoutX(140);
        enchereBT.setLayoutY(130);
        enchereBT.setStyle("-fx-background-color: lightgrey;-fx-border-style: solid;-fx-border-width: 2px;-fx-border-color: black");
        enchereBT.setOnMouseClicked(mouseEvent -> {
            if (!activerEnchere) {
                activerEnchere = true;
                enchereBT.setEffect(new Effets().putInnerShadow(Color.BLACK));
                enchereBT.setText("Enchères : OUI");
            } else {
                activerEnchere = false;
                enchereBT.setEffect(null);
                enchereBT.setText("Enchères : NON");
            }
        });
        enchereBT.setOnMouseEntered(mouseEvent -> {
            if (!activerEnchere) {
                enchereBT.setEffect(new Effets().putInnerShadow(Color.BLACK));
            }
        });
        enchereBT.setOnMouseExited(mouseEvent -> {
            if (!activerEnchere) {
                enchereBT.setEffect(null);
            }
        });
        return enchereBT;
    }

    /**
     * Génère le bouton pour afficher les évènements.
     *
     * @return button affichable
     */
    private Button boutonEvenement() {
        // Demande l'utilisation des évènements aléatoires
        Button eventBT = new Button("Évènements : NON");
        eventBT.setMinSize(120, 50);
        eventBT.setLayoutX(140);
        eventBT.setLayoutY(190);
        eventBT.setStyle("-fx-background-color: lightgrey;-fx-border-style: solid;-fx-border-width: 2px;-fx-border-color: black");
        eventBT.setOnMouseClicked(mouseEvent -> {
            if (!Evenement.activerEvenement) {
                Evenement.activerEvenement = true;
                eventBT.setEffect(new Effets().putInnerShadow(Color.BLACK));
                eventBT.setText("Évènements : OUI");
            } else {
                Evenement.activerEvenement = false;
                eventBT.setEffect(null);
                eventBT.setText("Évènements : NON");
            }
        });
        eventBT.setOnMouseEntered(mouseEvent -> {
            if (!Evenement.activerEvenement) {
                eventBT.setEffect(new Effets().putInnerShadow(Color.BLACK));
            }
        });
        eventBT.setOnMouseExited(mouseEvent -> {
            if (!Evenement.activerEvenement) {
                eventBT.setEffect(null);
            }
        });
        return eventBT;
    }

    /**
     * Génère bouton pour utiliser l'algorithme de Dijkstra.
     *
     * @return button affichage
     */
    private Button boutonDijkstra() {
        // Demande l'utilisation des évènements aléatoires
        Button dijkstraBT = new Button("Dijkstra : NON");
        dijkstraBT.setMinSize(120, 50);
        dijkstraBT.setLayoutX(270);
        dijkstraBT.setLayoutY(70);
        dijkstraBT.setStyle("-fx-background-color: lightgrey;-fx-border-style: solid;-fx-border-width: 2px;-fx-border-color: black");
        dijkstraBT.setOnMouseClicked(mouseEvent -> {
            if (!activerDijkstra) {
                activerDijkstra = true;
                dijkstraBT.setEffect(new Effets().putInnerShadow(Color.BLACK));
                dijkstraBT.setText("Dijkstra : OUI");
            } else {
                activerDijkstra = false;
                dijkstraBT.setEffect(null);
                dijkstraBT.setText("Dijkstra : NON");
            }
        });
        dijkstraBT.setOnMouseEntered(mouseEvent -> {
            if (!activerDijkstra) {
                dijkstraBT.setEffect(new Effets().putInnerShadow(Color.BLACK));
            }
        });
        dijkstraBT.setOnMouseExited(mouseEvent -> {
            if (!activerDijkstra) {
                dijkstraBT.setEffect(null);
            }
        });
        return dijkstraBT;
    }

    /**
     * Génère bouton de développement pour debug.
     *
     * @return button affichable
     */
    private Button boutonCheats() {
        Button button = new Bouton().creerBoutonBool("Cheats", 270, 130);
        button.setOnMouseClicked(mouseEvent -> {
            if (!activerCheats) {
                activerCheats = true;
                button.setEffect(new Effets().putInnerShadow(Color.BLACK));
                button.setText("Cheats : OUI");
            } else {
                activerCheats = false;
                button.setEffect(null);
                button.setText("Cheats : NON");
            }
        });
        button.setOnMouseEntered(mouseEvent -> {
            if (!activerCheats) {
                button.setEffect(new Effets().putInnerShadow(Color.BLACK));
            }
        });
        button.setOnMouseExited(mouseEvent -> {
            if (!activerCheats) {
                button.setEffect(null);
            }
        });
        return button;
    }

    /**
     * Génère bouton de limitation d'argent dans la partie.
     *
     * @return button affichable
     */
    private Button boutonArgent() {
        Button button = new Bouton().creerBoutonBool("Argent", 140, 250);
        button.setOnMouseClicked(mouseEvent -> {
            if (!activerArgent) {
                activerArgent = true;
                button.setEffect(new Effets().putInnerShadow(Color.BLACK));
                button.setText("Argent : OUI");
                group.getChildren().addAll(infoArgent, nbArgent);
            } else {
                activerArgent = false;
                button.setEffect(null);
                button.setText("Argent : NON");
                group.getChildren().removeAll(infoArgent, nbArgent);
                nbArgent.setText("");
            }
        });
        button.setOnMouseEntered(mouseEvent -> {
            if (!activerArgent) {
                button.setEffect(new Effets().putInnerShadow(Color.BLACK));
            }
        });
        button.setOnMouseExited(mouseEvent -> {
            if (!activerArgent) {
                button.setEffect(null);
            }
        });
        return button;
    }

    /**
     * Crée un bouton Exit.
     *
     * @return le bouton Exit
     */
    private Button boutonExit() {
        Button exit = new Bouton().creerBouton("Quitter");
        exit.setLayoutX(10);
        exit.setLayoutY(190);
        exit.setOnMouseClicked(mouseEvent -> primary.close());
        return exit;
    }

    /**
     * Crée un bouton Reset.
     * Relance le jeu une nouvelle aire
     *
     * @return le bouton Reset
     */
    private Button boutonReset() {
        Button reset = new Bouton().creerBouton("Nouvelle partie");
        reset.setLayoutX(10);
        reset.setLayoutY(130);
        reset.setOnMouseClicked(mouseEvent -> {
            setStatusPartie(false);
            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(300), ev -> {
                group.getChildren().clear();
                this.cleanPlateau();
                barre.reset();
                this.lancerPartie();
                this.aire = 0;
                argentPartie = 0;
                activerAnimation = false;
                activerEnchere = false;
                enchereTerminee = false;
                activerDijkstra = false;
                activerArgent = false;
                activerCheats = false;
                equipeSelectionne = null;
                personneSelectionne = null;
                barre.equipe1.setEffect(null);
                barre.equipe2.setEffect(null);
                for (Personne p : barre.getListeClasse()) {
                    p.getImagePerson().setEffect(null);
                }
                idEnchere.getAndSet(0);
                Collections.shuffle(Enchere.getListeEnchere());
            }));
            timeline.play();
        });
        return reset;
    }

    /**
     * Crée un bouton ReStart.
     * Relance le meme plateau
     *
     * @return le bouton ReStart
     */
    private Button boutonReStart() {
        Button reStart = new Bouton().creerBouton("Restart");
        reStart.setLayoutX(10);
        reStart.setLayoutY(70);
        reStart.setOnMouseClicked(mouseEvent -> {
            setStatusPartie(false);
            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(300), ev -> {
                group.getChildren().remove(0, group.getChildren().size());
                this.cleanPlateau();
                this.lancerScenePlateau();

                // Chargement de la sauvegarde faite à la fin des enchères
                if (activerEnchere) {
                    getE1().setArgent(BarrePersonnage.getSave().getArgentE1());
                    getE2().setArgent(BarrePersonnage.getSave().getArgentE2());
                    BarrePersonnage.listeEquipe1 = BarrePersonnage.getSave().getListeClasseE1();
                    BarrePersonnage.listeEquipe2 = BarrePersonnage.getSave().getListeClasseE2();
                }

                equipeSelectionne = null;
                personneSelectionne = null;
                barre.equipe1.setEffect(null);
                barre.equipe2.setEffect(null);
                if (activerEnchere) {
                    equipeSelectionne = Equipe.e1;
                    barre.getButtonTeamSelect().setEffect(new Effets().putInnerShadow(Plateau.equipeSelectionne.getCouleur()));
                }
                for (Personne p : barre.getListeClasse()) {
                    p.getImagePerson().setEffect(null);
                }
                barre = new BarrePersonnage();
                barre.majBarreEnchere();
                barre.cleanEffects();
            }));
            timeline.play();
        });
        return reStart;
    }

    /**
     * Crée un bouton PausePlay.
     * gere l'état et l'avancement du jeu
     *
     * @return un groupe contenant les boutons Play et Pause
     */
    private Group boutonPausePlay() {
        Group boutonGame = new Group();
        Label labelPlay = new Label("");
        labelPlay.setLayoutY(670);
        Label labelPause = new Label("");
        labelPause.setText("La partie est en pause");
        labelPause.setLayoutY(650);
        Button pause = new Bouton().creerBouton("Pause");
        Button play = new Bouton().creerBouton("Play");
        play.setLayoutX(140);
        play.setLayoutY(10);
        play.setOnMouseClicked(mouseEvent -> {
            if (!Equipe.e1.getTeam().isEmpty() && !Equipe.e2.getTeam().isEmpty()) {
                boutonGame.getChildren().remove(labelPause);
                boutonGame.getChildren().remove(play);
                boutonGame.getChildren().add(pause);
                setStatusPartie(true);
                if (group.getChildren().contains(barre.returnBarre())) {
                    group.getChildren().remove(barre.returnBarre());
                    scene.setFill(Color.DARKGRAY);
                    Plateau.scene.setCursor(Cursor.DEFAULT);

                    if (activerEnchere) {
                        for (Case c : Case.listeCase) {
                            c.getHexagone().setImage(Case.hexagone_img1);
                        }
                        for (Personne p : BarrePersonnage.listeClasse) {
                            p.getImagePerson().setEffect(null);
                        }
                    }

                }
                tour();
            } else if (!Personne.personnages.isEmpty() && (Equipe.e1.getTeam().isEmpty() || Equipe.e2.getTeam().isEmpty())) {
                Text attention = new Text("Il n'y qu'une seule equipe sur le terrain");
                attention.setY(650);
                attention.setX(20);
                attention.underlineProperty().setValue(true);
                attention.setFill(Color.RED);
                Plateau.group.getChildren().add(attention);
                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1500), ev -> Plateau.group.getChildren().remove(attention)));
                timeline.play();
            } else {
                Text attention = new Text("Veuillez remplir les hexagones avec des personnages");
                attention.setY(650);
                attention.setX(20);
                attention.underlineProperty().setValue(true);
                attention.setFill(Color.RED);
                Plateau.group.getChildren().add(attention);
                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1500), ev -> Plateau.group.getChildren().remove(attention)));
                timeline.play();
            }
        });

        pause.setLayoutX(140);
        pause.setLayoutY(10);
        pause.setOnMouseClicked(mouseEvent -> {
            boutonGame.getChildren().add(labelPause);
            boutonGame.getChildren().remove(pause);
            boutonGame.getChildren().add(play);
            setStatusPartie(false);
            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(400), ev -> {
                if (activerEnchere) {
                    brouillard();
                }
                if (!group.getChildren().contains(barre.returnBarre())) {
                    group.getChildren().add(barre.returnBarre());
                    scene.setFill(Color.WHITE);
                }
            }));
            timeline.play();

        });
        boutonGame.getChildren().addAll(play, labelPlay);
        return boutonGame;
    }

    /**
     * Crée des boutons modifiants la vitesse d'exécution d'un tour en millisecondes.
     *
     * @return les boutons de vitesse
     */
    private Button vitesseX1() {
        Button vitesseX1 = new Bouton().creerBouton("x1");
        vitesseX1.setLayoutX(270);
        vitesseX1.setLayoutY(70);
        vitesseX1.setEffect(new Effets().putInnerShadow(Color.BLACK));
        return vitesseX1;
    }

    private Button vitesseX2() {
        Button vitesseX2 = new Bouton().creerBouton("x2");
        vitesseX2.setLayoutX(270);
        vitesseX2.setLayoutY(vitesseX1().getLayoutY() + 60);
        return vitesseX2;
    }

    private Button vitesseX3() {
        Button vitesseX3 = new Bouton().creerBouton("x3");
        vitesseX3.setLayoutX(270);
        vitesseX3.setLayoutY(vitesseX2().getLayoutY() + 60);
        return vitesseX3;
    }

    /**
     * Crée un bouton affichant la vie et le nom des personnages.
     */
    private void afficheBarVie() {
        Button barVie = new Bouton().creerBouton("Afficher barres de vie");
        barVie.setMinSize(190, 50);
        barVie.setLayoutX(longueurMax - 200);
        barVie.setLayoutY(90);
        barVie.setOnMouseClicked(mouseEvent -> {
            if (!Personne.personnages.isEmpty()) {
                Personne.barreVisible = !Personne.barreVisible;
                for (Personne personnage : Personne.personnages) {
                    if (!personnage.getPosition().verifNoir()) {
                        personnage.afficherSanteEtNom();
                    }
                }
            }
        });
        group.getChildren().add(barVie);
    }

    /**
     * Menu contenant tout les boutons précédents et l'ajout au groupe général.
     */
    private void ajouteLesModes() {
        Button modes = new Bouton().creerBouton("Modes");
        modes.setLayoutX(140);
        modes.setLayoutY(10);

        Button animation = boutonAnimation();
        Button enchere = boutonEnchere();
        Button event = boutonEvenement();
        Button argent = boutonArgent();

        modes.setOnMouseClicked(mouseEvent -> {
            if (!group.getChildren().contains(animation)) {
                try {
                    group.getChildren().addAll(animation, enchere, event, argent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    group.getChildren().removeAll(animation, enchere, event, argent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        group.getChildren().add(modes);
    }

    /**
     * methode créant un bouton "Options" permettant d'utiliser le Dijkstra et les cheats.
     */
    private void ajouteLesOptions() {
        Button options = new Bouton().creerBouton("Options");
        options.setLayoutX(270);
        options.setLayoutY(10);

        Button dijkstra = boutonDijkstra();
        Button cheats = boutonCheats();

        options.setOnMouseClicked(mouseEvent -> {
            if (!group.getChildren().contains(dijkstra)) {
                try {
                    group.getChildren().addAll(dijkstra, cheats);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    group.getChildren().removeAll(dijkstra, cheats);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        group.getChildren().add(options);
    }

    /**
     * Menu contenant tout les boutons précédents et l'ajout au groupe général.
     */
    private void ajouteLeMenu() {
        Button menu = new Bouton().creerBouton("Menu");
        menu.setLayoutX(10);
        menu.setLayoutY(10);
        Button exit = boutonExit();
        Button reset = boutonReset();
        Button reStart = boutonReStart();
        menu.setOnMouseClicked(mouseEvent -> {
            if (!group.getChildren().contains(exit)) {
                try {
                    group.getChildren().addAll(reset, reStart, exit);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    group.getChildren().removeAll(reset, reStart, exit);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Button vitesse = new Bouton().creerBouton("Vitesse");
        vitesse.setLayoutX(270);
        vitesse.setLayoutY(10);
        Button x1 = vitesseX1();
        Button x2 = vitesseX2();
        Button x3 = vitesseX3();
        x1.setOnMouseClicked(mouseEvent -> {
            temps = 500;
            x1.setEffect(new Effets().putInnerShadow(Color.BLACK));
            x2.setEffect(null);
            x3.setEffect(null);
        });
        x2.setOnMouseClicked(mouseEvent -> {
            temps = 250;
            x2.setEffect(new Effets().putInnerShadow(Color.BLACK));
            x1.setEffect(null);
            x3.setEffect(null);
        });
        x3.setOnMouseClicked(mouseEvent -> {
            temps = 166;
            x3.setEffect(new Effets().putInnerShadow(Color.BLACK));
            x1.setEffect(null);
            x2.setEffect(null);
        });
        vitesse.setOnMouseClicked(mouseEvent -> {
            if (!group.getChildren().contains(x1)) {
                try {
                    group.getChildren().addAll(x1, x2, x3);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    group.getChildren().removeAll(x1, x2, x3);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        if (!activerEnchere || enchereTerminee) {
            boutonPausePlay();
            afficheBarVie();
            group.getChildren().add(vitesse);
        }
        group.getChildren().add(menu);
    }

    /**
     * Lance un tour : fait combattre les personnages en fonction d'un certain temps d'attente (vitesse) jusqu'à ce qu'il ne reste qu'une équipe ou
     * que l'utilisateur mette pause.
     */
    public void tour() {
        if (!Equipe.e1.getTeam().isEmpty() && !Equipe.e2.getTeam().isEmpty() && statusPartie) {
            tour++;
            System.out.println("Tour : " + tour);
            updateNbTour();

            for (Case c : Case.listeCase) {
                c.devenirBlanc();
            }

            // Gestion des événements aléatoires
            if (Evenement.activerEvenement) {
                Evenement.generationEvenements();
            }

            // Gestion des invocations
            Personne.personnages.addAll(Personne.invocationAttente);
            Personne.invocationAttente.clear();

            // Gestion des cases altérées
            if (!Case.listeCaseAlterees.isEmpty()) {
                for (Case c : Case.listeCaseAlterees) {
                    if (c.getAlteration() != null) {
                        c.getAlteration().passeTour();
                    } else {
                        Alteration.AlterSupprimer.add(c);
                    }
                }
                Alteration.nettoieCaseAlter();
            }

            // Gestion de l'affichage de barres de vie
            if (Personne.barreVisible && !Personne.personnages.isEmpty()) {
                for (Personne personnage : Personne.personnages) {
                    personnage.afficherSanteEtNom();
                }
            }

            // Gestion de l'ordre d'action des personnages
            Collections.shuffle(Personne.personnages);
            // Fait combattre les personnages non contenu dans mort
            for (Personne personnage : Personne.personnages) {
                if (personnage.getHealth() <= 0) {
                    Personne.morts.add(personnage);
                }
                if (!Personne.morts.contains(personnage)) {
                    personnage.getAlteration();
                    personnage.gainCD();
                    personnage.clearStatus();
                    /*if (personnage.getPosition().getAlteration() != null) {
                        if (personnage.getStatus().equals("freeze") && personnage.getClass().equals(Archimage.class)) {
                            personnage.action();
                        }
                    } else*/
                    if (personnage.getStatus().equals("normal")) {
                        personnage.action();
                    }
                }
            }

            // Gestion des morts
            for (Personne p : Personne.morts) {
                if (argentPartie > 0) {
                    p.getOtherTeam().setArgent(p.getOtherTeam().getArgent() + 50);
                }
                p.selfDelete();
                p.getTeam().setNbPersonne();
            }
            System.out.println("Nombre de morts durant ce tour : " + Personne.morts.size());
            System.out.println("Equipe 1 : " + Equipe.e1.getTeam().size() + " | Equipe 2 : " + Equipe.e2.getTeam().size());
            System.out.println("Nombre de personnages sur le plateau : " + Personne.personnages.size());
            Personne.morts.clear();

            // Relance le prochain tour (dans un certain temps --> vitesse)
            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(temps), ev -> tour()));
            timeline.play();

            // Change l'interface car nous somme en jeu
            if (Equipe.e1.getTeam().isEmpty() || Equipe.e2.getTeam().isEmpty()) {
                setStatusPartie(false);
                if (activerEnchere) {
                    brouillard();
                }
                scene.setFill(Color.WHITE);
                group.getChildren().remove(boutonPausePlay());
                group.getChildren().add(boutonPausePlay());
                group.getChildren().add(barre.returnBarre());
                Personne.morts.clear();
                Personne.personnages.addAll(Personne.invocationAttente);
            }
            if (argentPartie > 0) {
                getE1().setArgent(getE1().getArgent() + 25);
                getE2().setArgent(getE2().getArgent() + 25);
            }

            // Mise à jours du nombre de personne sur le plateau
            afficherNbPersonne();
        }
    }

    /**
     * Méthode qui va nettoyer le plateau en cas de reset / restart.
     */
    public void cleanPlateau() {
        tour = 0;
        Personne.personnages.clear();
        Personne.morts.clear();
        Case.listeCase.clear();
        Personne.invocationAttente.clear();
        getE1().getTeam().clear();
        getE2().getTeam().clear();
        scene.setFill(Color.WHITE);
        prix.setText("");
    }

    private void cheats() {
        scene.setOnKeyPressed(key -> {
            if (equipeSelectionne != null && !statusPartie) {
                if (key.getCode() == KeyCode.M && argentPartie > 0) {
                    equipeSelectionne.setArgent(equipeSelectionne.getArgent() + 100);
                } else if (key.getCode() == KeyCode.V) {
                    for (Personne p : Personne.personnages) {
                        if (p.getTeam() == equipeSelectionne) {
                            p.setHealth(10000);
                            p.setMaxHealth(10000);
                        }
                    }
                }
            }
        });
    }
}
