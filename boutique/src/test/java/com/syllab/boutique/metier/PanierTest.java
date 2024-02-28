package com.syllab.boutique.metier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.syllab.boutique.NommageRoyOsherove;
import com.syllab.boutique.metier.reducs.ProduitOffert;
import com.syllab.boutique.metier.reducs.ReducSeuil;

@DisplayNameGeneration(NommageRoyOsherove.class)
public class PanierTest {
  
  public final static String REDUC_CODE = "5POUR50";
  public final static double REDUC_MONTANT = 5;
  public final static double REDUC_SEUIL = 50;

  public final static String OFFERT_CODE = "PX3+1";
  public final static String OFFERT_REF = "PX";
  public final static int OFFERT_QTE = 4;

  static void referencerLesCoupons() {
    Panier.referencerCoupon(OFFERT_CODE, new ProduitOffert(OFFERT_REF, OFFERT_QTE));
    Panier.referencerCoupon(REDUC_CODE , new ReducSeuil(REDUC_MONTANT, REDUC_SEUIL));
  }

  // Initialisation
  // - Usuel
  @Test
  void initialisation_panierVide() {
    var p = new Panier();

    assertTrue(p.estVide());
    assertEquals(0, p.getPrixTotal(), 0.0001);
    assertFalse(p.getLignes().iterator().hasNext(), "Présence d'une ligne sur un nouveau panier");
  }
  // - Extrême (aucun)
  // - Erreur (aucun)

  // Ajouter
  // - Usuel
  @Test
  void ajouter_1Produit() {
    // Arrange
    var p = new Produit("AT12", "Ciment", 2);
    var panier = new Panier();

    // Act
    panier.ajouter(p, 3);

    // Assertion
    assertEquals(6, panier.getPrixTotal(), 0.0001);
    assertFalse(panier.estVide());
  }

  @Test
  void ajouter_2ProduitsDifferents() {
    var p1 = new Produit("P1", "L1", 2);
    var p2 = new Produit("P2", "L2", 5);
    var panier = new Panier();

    var l1 = panier.ajouter(p1, 3);
    var l2 = panier.ajouter(p2, 1);

    assertIterableEquals(Arrays.asList(l1, l2), panier.getLignes());
    assertEquals(11, panier.getPrixTotal(), 0.0001);
    assertFalse(panier.estVide());
  }

  @Test
  @DisplayName("(ajouter) 1 produit 2 fois -> additionne les quantités")
  void ajouter_1Produit2Fois_AdditionneLesQuantites() {
    var p1 = new Produit("P1", "L1", 2);
    var panier = new Panier();
    var l = panier.new Ligne(p1, 4);

    panier.ajouter(p1, 3);
    panier.ajouter(p1, 1);

    assertIterableEquals(Arrays.asList(l), panier.getLignes());
    assertEquals(8, panier.getPrixTotal(), 0.0001);
    assertFalse(panier.estVide());
  }

  // - Extreme (aucun)
  // - Erreur
  @Test
  void ajouter_quantite0_leveIllegalArgumentException() {
    var p1 = new Produit("P1", "L1", 2);
    var panier = new Panier();

    Executable act = () -> panier.ajouter(p1, 0);

    assertThrows(IllegalArgumentException.class, act);
  }

  @Test
  void ajouter_quantiteNegative_leveIllegalArgumentException() {
    var p1 = new Produit("P1", "L1", 2);
    var panier = new Panier();

    Executable act = () -> panier.ajouter(p1, -3);

    assertThrows(IllegalArgumentException.class, act);
  }

  @Test
  void ajouter_produitNull_leveNullPointerException() {
    var panier = new Panier();

    Executable act = () -> panier.ajouter(null, 2);

    assertThrows(NullPointerException.class, act);
  }

  // Diminuer
  // - Usuel
  @Test
  void diminuer_produitEnQuantite2OuPlus() {
    var p = new Produit("P1", "L1", 2);
    var panier = new Panier();

    panier.ajouter(p, 3);

    panier.diminuer(p);

    assertEquals(4, panier.getPrixTotal(), 0.0001);
    assertFalse(panier.estVide());
  }

  // - Extreme
  @Test
  void diminuer_dernierProduitEnQuantite1_panierVide() {
    var p = new Produit("P1", "L1", 2);
    var panier = new Panier();

    panier.ajouter(p, 1);

    panier.diminuer(p);

    assertEquals(0, panier.getPrixTotal(), 0.0001);
    assertTrue(panier.estVide());
  }

  @Test
  void diminuer_avantDernierProduitEnQuantite1_retireLeProduit() {
    var p1 = new Produit("P1", "L1", 2);
    var p2 = new Produit("P2", "L2", 5);
    var panier = new Panier();

    panier.ajouter(p1, 3);
    panier.ajouter(p2, 1);

    panier.diminuer(p2);

    assertEquals(6, panier.getPrixTotal(), 0.0001);
    assertFalse(panier.estVide());
  }

  // - Erreur
  @Test
  void diminuer_produitAbsentDuPanier_leveIllegalArgumentException() {
    var p1 = new Produit("P1", "L1", 2);
    var p2 = new Produit("P2", "L2", 5);
    var panier = new Panier();

    panier.ajouter(p1, 3);

    Executable act = () -> panier.diminuer(p2);

    assertThrows(IllegalArgumentException.class, act);
  }

  @Test
  void appliquerReduction_Total60Coupon5Pour50_Total55() {
    referencerLesCoupons();
    var panier = new Panier();

    panier.ajouter(new Produit("P1", "L1", 30), 2);

    panier.appliquerReduction(REDUC_CODE);

    assertEquals(55, panier.getPrixTotal(), 0.0001);
  }

  @Test
  void appliquerReduction_Total60CouponNonValide_Total60() {
    referencerLesCoupons();
    var panier = new Panier();

    panier.ajouter(new Produit("P1", "L1", 30), 2);

    panier.appliquerReduction("INVALIDE");

    assertEquals(60, panier.getPrixTotal(), 0.0001);
  }

  @Test
  void appliquerReduction_Total30Coupon5Pour50_Total30() {
    referencerLesCoupons();
    var panier = new Panier();

    panier.ajouter(new Produit("P1", "L1", 30), 1);

    panier.appliquerReduction(REDUC_CODE);

    assertEquals(30, panier.getPrixTotal(), 0.0001);
  }

  @Test
  void appliquerReduction_PX3Plus1Avec4PX_1PXOffert() {
    referencerLesCoupons();
    var panier = new Panier();
    var ligne = panier.ajouter(new Produit(OFFERT_REF, "LX", 20), 4);

    panier.ajouter(new Produit("P2", "L2", 1), 4);

    panier.appliquerReduction(OFFERT_CODE);

    assertEquals(4, ligne.getQuantite());
    assertEquals(60, ligne.getPrixTotal(), 0.0001);
    assertEquals(60 + 4, panier.getPrixTotal(), 0.0001);
  }

  @Test
  void appliquerReduction_PX3Plus1Et5Pour50Total60_PXOffertEtTotal55() {
    referencerLesCoupons();
    var panier = new Panier();

    var ligne = panier.ajouter(new Produit(OFFERT_REF, "LX", 20), 4);

    panier.appliquerReduction(REDUC_CODE);
    panier.appliquerReduction(OFFERT_CODE);

    assertEquals(4, ligne.getQuantite());
    assertEquals(60, ligne.getPrixTotal(), 0.0001);
    assertEquals(55, panier.getPrixTotal(), 0.0001);
  }

  @Test
  void appliquerReduction_PX3Plus1Avec9PX_2PXOfferts() {
    referencerLesCoupons();
    var panier = new Panier();

    panier.ajouter(new Produit("P2", "L2", 1), 4);
    panier.appliquerReduction(OFFERT_CODE);

    var ligne = panier.ajouter(new Produit(OFFERT_REF, "LX", 20), 9);

    assertEquals(9, ligne.getQuantite());
    assertEquals(140, ligne.getPrixTotal(), 0.0001);
    assertEquals(140 + 4, panier.getPrixTotal(), 0.0001);
  }

  @Test
  void appliquerReduction_PX3Plus1Avec3PX_PasDePXOffert() {
    referencerLesCoupons();
    var panier = new Panier();
    var ligne = panier.ajouter(new Produit(OFFERT_REF, "LX", 20), 3);

    panier.ajouter(new Produit("P2", "L2", 1), 4);

    panier.appliquerReduction(OFFERT_CODE);

    assertEquals(3, ligne.getQuantite());
    assertEquals(60, ligne.getPrixTotal(), 0.0001);
    assertEquals(60 + 4, panier.getPrixTotal(), 0.0001);
  }

}