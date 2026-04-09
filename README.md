# App Tensionnel Android

Une application Android intuitive et accessible conçue pour aider les patients hypertendus (et leurs aidants) à suivre quotidiennement leur santé cardiovasculaire.

## Objectifs du Projet
L'objectif principal est de simplifier la saisie et le suivi de la tension artérielle pour un public senior ou non technophile. L'interface privilégie la lisibilité avec de grands boutons et des contrastes clairs.

## Aperçu de l'interface (Maquettes)

| Accueil & Saisie | Tendances & Graphiques | Historique des mesures | Paramètres |
|:---:|:---:|:---:|:---:|
| ![Home](img/Home1.png) | ![Tendance](img/Tendance1.png) | ![Historique](img/Historique1.png) | ![Paramètres](img/Parametres1.png) |
| *Saisie simplifiée* | *Suivi visuel* | *Gestion des données* | *Rappels personnalisés* |

---

## Fonctionnalités Principales (MVP)

### 1. Saisie des Mesures
- Enregistrement rapide : Tension Systolique, Diastolique et Pouls.
- **Règles métier :**
  - **Alerte Critique :** Alerte immédiate (popup + vibration) si Systolique > 180 ou Diastolique > 120.
  - **Alerte Vigilance :** Notification si le pouls est < 50 ou > 120 bpm.
- Ajout de notes optionnelles pour chaque mesure.

### 2. Suivi & Graphiques
- Visualisation de l'évolution sous forme de courbes (systolique/diastolique).
- Filtres temporels : 7 jours, 30 jours ou 90 jours.
- Détails interactifs en cliquant sur les points du graphique.

### 3. Historique & Gestion
- Liste exhaustive de toutes les mesures.
- Suppression facilitée via un geste de balayage (swipe-to-delete).
- Édition d'une mesure par appui prolongé.

### 4. Export & Partage
- Génération d'un fichier CSV contenant l'intégralité des données.
- Partage direct avec un professionnel de santé via les outils de communication installés (Email, Messagerie).

### 5. Rappels Quotidiens
- Système de notifications programmables pour assurer la régularité des prises de mesure.
- Option de désactivation disponible dans les paramètres.

---

## Caractéristiques Techniques
- **Langage :** Kotlin
- **Persistance des données :** SharedPreferences (Stockage local sécurisé)
- **Bibliothèque graphique :** MPAndroidChart
- **Gestion des tâches :** WorkManager & NotificationManager
- **Confidentialité :** Fonctionnement autonome hors ligne (aucune donnée n'est transmise à un serveur tiers).

## Procédure d'Installation
1. Cloner le dépôt : `git clone https://github.com/RayanBT/App_Tensionnel_Android.git`
2. Ouvrir le projet dans **Android Studio**.
3. Synchroniser les dépendances Gradle et exécuter l'application sur un terminal Android (Min SDK 24+).

---
*Projet réalisé dans le cadre du module Mobile - E4 Ecole d'Ingénieur.*