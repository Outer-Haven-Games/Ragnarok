package outerhaven.Entites.Personnages.PersonnagesPrime;

import javafx.scene.image.Image;
import javafx.scene.text.Text;
import outerhaven.Case;
import outerhaven.Entites.Personnages.PersonnagesMagiques.Paladin;
import outerhaven.Entites.Personnages.Personne;
import outerhaven.Equipe;

public class PaladinPrime extends Paladin {

    public PaladinPrime() {
        this.augmenterStats(2);
    }

    public PaladinPrime(Equipe team, Case position) {
        super(team, position);
        this.augmenterStats(2);
    }

    @Override
    public Personne personneNouvelle(Equipe team, Case position) {
        return new PaladinPrime(team, position);
    }

    @Override
    public Text getinfoTitleText() {
        return new Text("PaladinPrime (" + this.getCost() + " €) :\n");
    }

    @Override
    public Text getinfoDescText() {
        return new Text("\nAttaque augmentées, vie et armure plus élevées mais portée réduite.\nGagne 25 de mana par tour.\nPeut se soigner 1 quart de sa vie pour 50 de mana." + "\n" +
                "- PV : " + this.getHealth() + "\n" +
                "- Mana : " + this.getMana() + "\n" +
                "- Armure : " + this.getArmor() + "\n" +
                "- Dégâts : " + this.getDamage() + "\n" +
                "- Portée : " + this.getRange() + "\n");
    }

    @Override
    public Image getImageFace() {
        return new Image(PaladinPrime.class.getResourceAsStream("/Images/Personnes/PaladinPrime.png"));
    }

    /**
     * Se soigne et devient plus résistant plus il reste sur le plateau
     */
    @Override
    public void soigner(double vie) {
        if (this.getHealth() <= this.getMaxHealth() - vie - 500) {
            this.setHealth(this.getHealth() + vie + 500);
        } else if (this.getHealth() > this.getMaxHealth() - vie - 500) {
            this.setHealth(this.getMaxHealth());
        }
        seRenforce(25);
    }
}
