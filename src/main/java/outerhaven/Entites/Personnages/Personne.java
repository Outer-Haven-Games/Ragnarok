package outerhaven.Entites.Personnages;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.util.Duration;
import outerhaven.Case;
import outerhaven.Entites.Entite;
import outerhaven.Entites.Personnages.Invocations.Invocation;
import outerhaven.Equipe;
import outerhaven.Interface.Effets;
import outerhaven.Mecaniques.Poste.Poste;
import outerhaven.Plateau;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static outerhaven.Plateau.*;

/**
 * Unité sur le plateau qui appartient à une équipe et se déplace de Case en Case. Elle peut posséder des caractéristiques spéciales.
 */

public abstract class Personne extends Entite {
    /**
     * Liste des noms stockés dans un fichier texte modifiable par n'importe qui pour ajouter de la personnalisation.
     */
    private static final ArrayList<String> listName = new ArrayList<>();
    /**
     * Liste de personnes présentes sur le plateau.
     */
    public static ArrayList<Personne> personnages = new ArrayList<>();
    /**
     * Liste de personnes mortes durant un combat.
     */
    public static ArrayList<Personne> morts = new ArrayList<>();
    /**
     * Liste d'invocation en attente pour les futurs unités à ajouter pour le prochain tour.
     */
    public static ArrayList<Personne> invocationAttente = new ArrayList<>();
    public static boolean barreVisible = false;
    private final String name; // Stocké dans un tableau.
    public Case casePrecedente;
    public double largeurMax = Screen.getPrimary().getVisualBounds().getHeight();
    public double longueurMax = Screen.getPrimary().getVisualBounds().getWidth();
    protected int damage;       // Dégâts de la personne
    protected int range;        // Portée d'attaque en nombre de case
    ArrayList<Case> pathToEnemy;
    private Group SanteNom = new Group();
    private double health;      // Vie de la personne
    private double maxHealth;   // Vie maximale de la personne qui est égale à sa vie en début de partie
    private double armor;       // Armure de la personne
    private double cost;        // Coût de la personne
    private int speed;          // Nombre de case qu'il parcourt chaque tour
    private Equipe team;        // Equipe de la personne
    private ImageView imageperson = new ImageView(this.getImageFace());
    private Case position;
    private int cooldown = 0;
    private String status = "normal";
    private int duréeStatus = 0;
    private Poste poste;

    public Personne(double health, double armor, double cost, int damage, int range, int speed) {
        this.name = getRandomName();
        this.health = health;
        this.maxHealth = health;
        this.armor = armor;
        this.cost = cost;
        this.damage = damage;
        this.range = range;
        this.speed = speed;
        if (!(this instanceof Invocation)) {
            personnages.add(this);
        }
    }

    public Personne(double health, double armor, double cost, int damage, int range, int speed, Equipe team) {
        this(health, armor, cost, damage, range, speed);
        this.team = team;
        if (!(this instanceof Invocation)) {
            this.team.getTeam().add(this);
        }
    }

    public Personne(double health, double armor, double cost, int damage, int range, int speed, Equipe team, Case position) {
        this(health, armor, cost, damage, range, speed, team);
        this.position = position;
        this.position.setContenu(this);
        this.casePrecedente = position;
    }

    /**
     * Fonction qui permet d'obtenir un nom aléatoire parmi ceux dans la liste des noms disponibles.
     *
     * @return un nom aléatoire pour la personne
     */
    public static String getRandomName() {
        if (listName.isEmpty()) {
            ajouteNom();
        }
        return listName.get(new Random().nextInt(listName.size()));
    }

    /**
     * Méthode permettant d'ajouter dans la liste des nom les noms écris dans "noms.txt"
     */
    private static void ajouteNom() {
        Scanner scan = new Scanner(Personne.class.getResourceAsStream("/Texts/noms.txt"));
        int ligne = 1;
        while (scan.hasNextLine()) {
            String nom = scan.nextLine();
            listName.add(nom);
            ligne++;
        }
        scan.close();
    }

    /**
     * Action basique de toute personne en fonction de la distance avec sa cible la plus proche : attaquer ou se déplacer.
     */
    public void action() {
        this.genererChemin(); // Créer le chemin vers l'objectif de this (attaquer, protéger un bâtiment ...)
        this.comportementsBasiques(); // Se déplace ou attaque.
    }

    public void genererChemin() {
        if (this.getPoste() != null) {
            this.pathToEnemy = this.getPoste().calculerChemin(this); // Calcule le chemin en fonction de plusieurs paramètres et du poste de this.
        } else {
            this.pathToEnemy = calculerChemin(); // Calcule le chemin en fonction de plusieurs paramètres.
        }
    }

    /**
     * Fonction qui permet d'obtenir le chemin le plus court vers l'adversaire le plus proche en fonction de divers paramètres sur le Plateau.
     */
    public ArrayList<Case> calculerChemin() {
        ArrayList<Case> chemin;
        if (activerDijkstra && this.position.nbVoisinsLibres() > 0) {
            // Chemin calculé avec l'algorithme de Dijkstra si option activée.
            chemin = this.position.pathDijkstra();
        } else {
            // Sinon calcul du chemin avec l'utilisation du calcul vectoriel lambda.
            chemin = this.position.pathToPerso(getOtherTeam());
        }
        if (activerDijkstra && chemin.size() <= 0) {
            // Si le personnage (this) est bloqué où a son champ de vision bloqué (ligne d'alliés) on utilise le calcul vectoriel.
            chemin = this.position.pathToPerso(getOtherTeam());
        }
        return chemin;
    }

    public void comportementsBasiques() {
        try {
            if (this.pathToEnemy.size() - 1 <= this.getRange()) {
                // Si l'ennemi le plus proche est dans la portée d'attaque de this alors il l'attaque.
                attaquer(this.pathToEnemy.get(this.pathToEnemy.size() - 1).getContenu().get(0));
            } else {
                // Sinon il se déplace pour se rapprocher de lui.
                deplacer(this.pathToEnemy.get(this.getSpeed()));
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println(this.getName() + " : " + e.getMessage());
        }
    }

    /**
     * Méthode qui permet d'attaquer le personnage mit en paramètre s'il est d'une équipe différente.
     */
    public void attaquer(Personne p) {
        if (this.getTeam() != p.getTeam()) {
            double damageMultiplier = this.damage / (this.damage + this.armor / 5);
            double totalDamage = this.damage * damageMultiplier;
            p.prendreDégâts(totalDamage);
        }
    }

    /**
     * Méthode qui permet de se déplacer dans une case mis en paramètre et vidant la case précédente avec une animation.
     */
    public void deplacer(Case fin) {
        // Debug pathfinding
        this.setCasePrecedente(this.getPosition());
        if (!isActiverAnimation()) {
            Case casePrecedente = this.getPosition();
            this.setPosition(fin);
            fin.rentrePersonnage(this);
            casePrecedente.seVider();
            if (Personne.barreVisible) {
                this.afficherSanteEtNom();
            }
        } else {
            double fps = 15;
            fin.getContenu().add(this);
            Case casePrecedente = this.position;
            Group affichageCaseprecedente = this.affichagePersonnage();
            this.setPosition(fin);
            group.getChildren().add(affichageCaseprecedente);
            casePrecedente.seVider();
            AtomicReference<Double> x = new AtomicReference<>(affichageCaseprecedente.getLayoutX());
            AtomicReference<Double> y = new AtomicReference<>(affichageCaseprecedente.getLayoutY());
            double xVec = (casePrecedente.getPosX() - fin.getPosX()) / fps;
            double yVec = (casePrecedente.getPosY() - fin.getPosY()) / fps;
            AtomicInteger count = new AtomicInteger(0);
            Timeline timeline = new Timeline(new KeyFrame(Duration.millis((temps - 100) / fps), ev -> {
                x.set(x.get() - xVec);
                y.set(y.get() - yVec);
                affichageCaseprecedente.setLayoutX(x.get());
                affichageCaseprecedente.setLayoutY(y.get());
                count.getAndIncrement();
                if (count.get() == fps) {
                    group.getChildren().remove(affichageCaseprecedente);
                    this.position = fin;
                    deplacementFinal(casePrecedente, fin);
                }
            }));
            timeline.setCycleCount(15);
            timeline.play();
        }
    }

    public boolean getDanger() {
        // Vérification si les cases voisines contiennent au moins un ennemi
        for (Case c : this.getPosition().voisinsLibres(false)) {
            // Si la case voisine n'est pas vide
            if (c.getContenu().size() != 0) {
                // Si le contenu de la case n'est pas un allié
                if (c.getContenu().get(0).getTeam() != this.getTeam()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Algorithme gérant l'animation finale des déplacements.
     */
    public void deplacementFinal(Case depart, Case fin) {
        fin.seViderPourAnimation();
        fin.rentrePersonnage(this);
        if (Personne.barreVisible) {
            this.afficherSanteEtNom();
        }
    }

    /**
     * Affiche les personnages dans la barre.
     *
     * @param i : paramètre pour la méthode genereBarre() de la classe BarrePersonnage afin de générer chaque personne ne la liste listClasse
     * @return un groupe contenant les personnages dans la barre avec les effets et leur description
     */
    public Group affichagePersonnageBarre(int i) {
        Group group = new Group();
        imageperson.setFitHeight(130);
        imageperson.setFitWidth(100);
        imageperson.setY(Screen.getPrimary().getVisualBounds().getHeight() - 160);
        imageperson.setX(50 + i * (imageperson.getFitWidth() + 50));
        // Création des itérations avec les images dans la barre.
        imageperson.setOnMouseEntered((mouseEvent) -> {
            group.getChildren().add(afficherInfo(imageperson.getX(), imageperson.getY() - 106));
        });
        imageperson.setOnMouseExited((mouseEvent) -> {
            group.getChildren().remove(1);
        });
        imageperson.setOnMouseClicked((mouseEvent) -> {
            if (equipeSelectionne != null) {
                Plateau.personneSelectionne = this;
                imageperson.setEffect(new Effets().putInnerShadow(equipeSelectionne.getCouleur()));
                for (Personne p : barre.getListeClasse()) {
                    if (personneSelectionne == p) {
                        p.getImagePerson().setEffect(new Effets().putInnerShadow(equipeSelectionne.getCouleur()));
                    } else {
                        p.getImagePerson().setEffect(null);
                    }
                }
            }
        });
        group.getChildren().add(imageperson);
        return group;
    }

    /**
     * Fonction permettant de créer la barre d'informations d'un personnage.
     *
     * @param X : position en X de la description d'une personne
     * @param Y : position en Y de la description d'une personne
     * @return un groupe contenant les informations et la description d'une personne
     */
    public Group afficherInfo(double X, double Y) {
        Group description = new Group();
        Rectangle barre = new Rectangle(425, 205, Color.LIGHTGRAY);
        barre.setX(X);
        barre.setY(Y - 150);
        barre.setStroke(Color.BLACK);
        barre.setStrokeWidth(2);

        Text title = this.getinfoTitleText();
        title.setStyle("-fx-font-weight: bold");
        title.setX(X + 10);
        title.setY(Y - 130);

        Text descT = this.getinfoDescText();
        descT.setX(X + 10);
        descT.setY(Y - 130);

        description.getChildren().add(barre);
        description.getChildren().add(title);
        description.getChildren().add(descT);
        return description;
    }

    /**
     * Fonctions abstraites devant être présentes dans toutes les classes filles.
     */
    public abstract Personne personneNouvelle(Equipe team, Case position);

    public abstract Text getinfoTitleText();

    public abstract Text getinfoDescText();

    public abstract Image getImageFace();

    /**
     * Méthode permettant d'afficher l'image de la personne.
     *
     * @return un groupe contenant son image
     */
    public Group affichagePersonnage() {
        Group group = new Group();
        if (!activerEnchere) {
            group.setEffect(new Effets().putInnerShadow(this.getTeam().getCouleur()));
        }
        group.getChildren().add(this.afficherImageFace());
        return group;
    }

    /**
     * Fonction permettant d'afficher le nom de la personne.
     *
     * @return un groupe contenant son nom avec la couleur de son équipe
     */
    public Group afficherNom() {
        Text name = new Text();
        name.setText(this.getName());
        name.setX(getPosition().getPosX() + 10);
        name.setY(getPosition().getPosY() + taille / 2.6);
        name.setStyle("-fx-font-weight: bold");
        name.setEffect(new Effets().putInnerShadow(this.team.getCouleur()));

        Group group = new Group();
        group.getChildren().add(name);
        return group;
    }

    /**
     * Fonction permettant d'afficher l'image d'une personne et ajoute la possibilité de le supprimer en cliquant dessus hors partie lancée (!statusPartie).
     *
     * @return un groupe contenant l'image de la personne
     */
    public Group afficherImageFace() {
        ImageView person = new ImageView(this.getImageFace());
        person.setFitHeight(taille / 1.5);
        person.setFitWidth(taille / 2);
        person.setX(position.getPosX() + taille / 3);
        person.setY(position.getPosY() - taille / 20);
        InnerShadow ombre = new InnerShadow();
        ombre.colorProperty().setValue(getTeam().getCouleur());
        person.setEffect(ombre);

        Group group = new Group();
        group.getChildren().add(person);

        // Cliquer sur l'image hors partie permet de supprimer la personne
        person.setOnMouseClicked((mouseEvent) -> {
            if (!statusPartie && !activerEnchere) {
                if (argentPartie != 0) {
                    position.getContenu().get(0).getTeam().setArgent(position.getContenu().get(0).getTeam().getArgent() + this.getCost());
                }
                selfDelete();
            }
        });

        // Affichage des case voisines en fonction de la portée de déplacement
        if (!statusPartie) {
            person.setOnMouseEntered((mouseEvent) -> {
                position.afficherCaseVoisines(speed, true);
            });

            person.setOnMouseExited((mouseEvent) -> {
                position.afficherCaseVoisines(speed, false);
            });
        }

        return group;
    }

    /**
     * Méthode permettant d'afficher le nom et la santé des personnes sur le plateau si pas affichés.
     */
    public void afficherSanteEtNom() {
        SanteNom.getChildren().clear();
        SanteNom.getChildren().addAll(afficherStats(), afficherNom());
        if (barreVisible && !position.verifNoir()) {
            supprimerSanteEtNom();
            group.getChildren().add(SanteNom);
        } else if (!barreVisible && group.getChildren().contains(SanteNom)) {
            supprimerSanteEtNom();
        } else if (position.verifNoir()) {
            supprimerSanteEtNom();
        }
    }

    /**
     * Méthode permettant d'enlever le nom et la santé des personnes de l'affichage du plateau si affichés.
     */
    public void supprimerSanteEtNom() {
        if (group.getChildren().contains(SanteNom)) {
            group.getChildren().remove(SanteNom);
        }
    }

    /**
     * Méthode permettant à une personne de disparaître complétement (vider sa case, l'enlever des listes le contenant ...).
     */
    public void selfDelete() {
        this.position.seVider();
        SanteNom.getChildren().clear();
        this.getTeam().getTeam().remove(this);
        personnages.remove(this);
    }

    /**
     * Fonction qui crée les barres de statistiques de chaque personne.
     */
    public Group afficherStats() {
        Group group = new Group();
        group.getChildren().addAll(afficherSante());
        return group;
    }

    /**
     * Fonction qui crée la barre de vie de chaque personne en fonction de sa vie restant en pourcentage.
     *
     * @return un groupe contenant la barre de vie pour une personne
     */
    public Group afficherSante() {
        Rectangle barre = new Rectangle(taille, taille / 10, Color.BLACK);
        Rectangle vie = new Rectangle(taille - 4, taille / 10 - 4, Color.RED);

        barre.setX(getPosition().getPosX());
        barre.setY(getPosition().getPosY() + taille / 2.2);

        vie.setY(barre.getY() + 2);
        vie.setX(barre.getX() + 2);

        double percentage = (this.getHealth() / maxHealth);
        double width = (percentage * (taille - 4));
        vie.widthProperty().setValue(width);

        Group group = new Group();
        group.getChildren().addAll(barre, vie);

        return group;
    }

    /**
     * Méthode permettant de prendre des dégâts
     *
     * @param dégâts que l'on va enlever à la cible
     */
    public void prendreDégâts(double dégâts) {
        this.setHealth(this.getHealth() - dégâts);
    }

    /**
     * Méthode qui permet à une personne d'avoir sa vie soignée
     *
     * @param vie que l'on veut soigner
     */
    public void soigner(double vie) {
        if (this.getHealth() <= this.getMaxHealth() - vie) {
            this.setHealth(this.getHealth() + vie);
        } else if (this.getHealth() > this.getMaxHealth() - vie) {
            this.setHealth(this.getMaxHealth());
        }
    }

    /**
     * Méthode qui permet à une personne de devenir plus résistante sur le plateau
     *
     * @param armure qu'on veut augmenter
     */
    public void seRenforce(double armure) {
        this.setArmor(this.getArmor() + armure);
    }

    public void augmenterStats(double multiplicateur) {
        this.health *= multiplicateur;
        this.maxHealth *= multiplicateur;
        this.damage *= multiplicateur;
        this.armor *= multiplicateur;
        if (this.range != 1) {
            this.range *= multiplicateur;
        }
        this.cost *= multiplicateur;
    }

    /**
     * Permet de mettre un status sur this (comme l'étourdir, geler ...).
     *
     * @param durée en nombre de tour du status
     */
    public void changeStatus(String status, int durée) {
        this.setStatus(status);
        this.setDuréeStatus(durée);
    }

    public void clearStatus() {
        if (this.duréeStatus > 0) {
            this.duréeStatus--;
            if (this.duréeStatus == 0) {
                this.setStatus("normal");
            }
        }
    }

    /**
     * Getters et setters divers
     */
    public String getName() {
        return name;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public double getArmor() {
        return armor;
    }

    public void setArmor(double armor) {
        this.armor = armor;
    }

    public double getCost() {
        return cost;
    }

    public int getDamage() {
        return damage;
    }

    public Case getPosition() {
        return position;
    }

    public void setPosition(Case position) {
        this.position = position;
    }

    public Equipe getTeam() {
        return team;
    }

    public void setTeam(Equipe team) {
        this.team = team;
    }

    public int getRange() {
        return range;
    }

    public int getSpeed() {
        return speed;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
    }

    public void getAlteration() {
        if (this.position.getAlteration() != null) {
            this.position.getAlteration().appliquerEffet(this);
        }
    }

    public ArrayList<Case> getPathToEnemy() {
        return pathToEnemy;
    }

    /**
     * Fonction qui permet d'obtenir l'équipe adverse à this
     *
     * @return l'équipe opposée à l'équipe de this.
     */
    public Equipe getOtherTeam() {
        if (Equipe.e1.equals(this.team)) {
            return Equipe.e2;
        } else {
            return Equipe.e1;
        }
    }

    public void gainCD() {
        this.cooldown++;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public int getDuréeStatus() {
        return duréeStatus;
    }

    public void setDuréeStatus(int duréeStatus) {
        this.duréeStatus = duréeStatus;
    }

    public ImageView getImagePerson() {
        return imageperson;
    }

    public void setImagePerson(ImageView imagePerson) {
        this.imageperson = imagePerson;
    }

    public ImageView getImagePersonPosition(int x, int y) {
        ImageView imagePosition = new ImageView();
        imagePosition.setImage(this.getImageFace());
        imagePosition.setX(x);
        imagePosition.setY(y);
        return imagePosition;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCasePrecedente(Case casePrecedente) {
        this.casePrecedente = casePrecedente;
    }

    public Poste getPoste() {
        return poste;
    }

    public void setPoste(Poste poste) {
        this.poste = poste;
    }

    @Override
    public String toString() {
        return this.getName() + " : " + this.getHealth() + "\n";
    }
}