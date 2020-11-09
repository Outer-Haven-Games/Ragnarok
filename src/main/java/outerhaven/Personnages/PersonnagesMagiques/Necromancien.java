package outerhaven.Personnages.PersonnagesMagiques;

import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import outerhaven.Case;
import outerhaven.Equipe;
import outerhaven.Personnages.Invocations.Mort;
import outerhaven.Personnages.Personne;
import outerhaven.Plateau;

import java.util.ArrayList;

public class Necromancien extends PersonneMagique {

    public Necromancien() {
        super(2000, 50, 1000, 100, 4, 1, 100);
    }

    public Necromancien(Equipe team, Case position) {
        super(2000, 50, 1000, 100, 4, 1, team, position, 100);
    }

    public void attaquer(Personne p) {
        double damageMultiplier = this.getDamage() / (this.getDamage() + this.getArmor() / 5);
        double totalDamage = this.getDamage() * damageMultiplier;
        if (this.getTeam() != p.getTeam()) {
            p.setHealth(p.getHealth() - totalDamage);

            // Vol de vie
            if (this.getHealth() < this.getMaxHealth() - totalDamage) {
                this.setHealth(this.getHealth() + totalDamage);
            } else if (this.getHealth() < this.getMaxHealth()) {
                this.setHealth(getHealth());
            }

            if (p.getHealth() <= 0) {
                System.out.println(p.getName() + " est mort !");
            }
        }
    }

    public void action() {
        this.gainMana();
        System.out.println("Nombre de case vide autour de " + this.getName() + " : " + this.getPosition().nbVoisinsLibres());
        if (this.getPosition().nbVoisinsLibres() == 0) {
            System.out.println(this.getName() + " patiente");
        } else {
            if (getPosition().pathToPerso(getOtherTeam()).size() == 0) {
                System.out.println(this.getName() + " patiente");
            } else if (this.getMana() > 100) {

                // Invocation de morts (marche pas encore)
                for (Case c : this.getPosition().voisinsLibres(true)) {
                    c.getContenu().add(new Mort(this.getTeam(), c));
                    c.setAffichagecontenu(c.getContenu().get(0).affichagePersonnage());
                    c.getContenu().get(0).afficherSanteEtNom();
                    Plateau.group.getChildren().add(c.getAffichagecontenu());
                    InnerShadow ombre = new InnerShadow();
                    ombre.colorProperty().setValue(this.getTeam().getCouleur());
                    c.getHexagone().setEffect(ombre);
                }
                this.setMana(this.getMana() - 100);

            } else {
                ArrayList<Case> pathToEnnemy = new ArrayList<>(this.getPosition().pathToPerso(getOtherTeam()));
                System.out.println("Taille du chemin vers l'ennemis le plus proche pour " + this.getName() + " : " + (pathToEnnemy.size() - 1));
                if (pathToEnnemy.size() - 1 <= this.getRange()) {
                    System.out.println(this.getName() + " (" + this.getHealth() + ") attaque " + pathToEnnemy.get(pathToEnnemy.size() - 1).getContenu().get(0).getName() + " (" + pathToEnnemy.get(pathToEnnemy.size() - 1).getContenu().get(0).getHealth() + ")");
                    attaquer(pathToEnnemy.get(pathToEnnemy.size() - 1).getContenu().get(0));
                } else {
                    System.out.println(this.getName() + " se déplace");
                    déplacer(pathToEnnemy.get(this.getSpeed()));
                }
                // System.out.println("Vie restante de la cible " + getHealth());
            }
        }
    }

    @Override
    public Personne personneNouvelle(Equipe team,Case position) {
        return new Necromancien(team,position);
    }

    @Override
    public Text getinfoTitleText() {
        return new Text("Nécromancien (" + this.getCost() + " €) :\n");
    }

    @Override
    public Text getinfoDescText() {
        return new Text("\nPortée et dégats augmentés mais vie et armure plus basses." + "\n" + "PV : " + this.getHealth() + "\n" + "Armure : " + this.getArmor() + "\n" + "Dégats : " + this.getDamage() + "\n");
    }

    @Override
    public Image getImageFace() {
        return new Image(Necromancien.class.getResourceAsStream("/Images/Necromancer.png"));
    }
}
