package outerhaven.Mecaniques;

import javafx.scene.Group;
import outerhaven.Equipe;
import outerhaven.Interface.BarrePersonnage;
import outerhaven.Personnages.Personne;
import outerhaven.Plateau;

import java.util.ArrayList;

public class Enchere {
    public static ArrayList<Enchere> listeEnchere = new ArrayList<>();
    private Personne produit;
    private double prixMinimal;
    private Equipe equipeGagnante;
    private boolean status;

    public Enchere(Personne produit) {
        this.produit = produit;
        this.prixMinimal = 0;
        this.status = true;
    }

    public void cloreEnchere() {
        this.setStatus(false);
        this.getProduit().setTeam(this.getEquipeGagnante());
        if (this.getEquipeGagnante() == Plateau.getE1()) {
            BarrePersonnage.listeEquipe1.add(this.getProduit());
        } else if (this.getEquipeGagnante() != null) {
            BarrePersonnage.listeEquipe2.add(this.getProduit());
        }
        //Enchere.listeEnchere.remove(this);
    }

    public Group afficherInformations() {
        return this.getProduit().afficherInfo(700, 500);
    }

    public static ArrayList<Enchere> getListeEnchere() {
        return listeEnchere;
    }

    public static void ajouterEnchere(Enchere e) {
        Enchere.listeEnchere.add(e);
    }

    public Personne getProduit() {
        return produit;
    }

    public void setProduit(Personne produit) {
        this.produit = produit;
    }

    public double getPrixMinimal() {
        return prixMinimal;
    }

    public Equipe getEquipeGagnante() {
        return equipeGagnante;
    }

    public void setEquipeGagnante(Equipe equipeGagnante) {
        this.equipeGagnante = equipeGagnante;
    }

    public void setPrixMinimal(double prixMinimal) {
        this.prixMinimal = prixMinimal;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}