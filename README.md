# Parkour

Plugin de parcours pour Paper 1.21. On pose un départ, des checkpoints et une arrivée en jeu,
le plugin gère le chrono, les classements et les hologrammes.

## Ce qu'il fait

- Autant de parcours que tu veux, chacun avec son départ, ses checkpoints et son arrivée
- Chrono à la milliseconde dans l'action bar, progression dans une bossbar
- Checkpoints dans l'ordre, retour au dernier en cas de chute, d'eau, de lave ou de vide
- Objets dans la barre : dernier checkpoint, recommencer, abandonner
- Classement par parcours, record perso, annonce quand quelqu'un prend la première place
- Hologrammes (text displays) au-dessus de chaque point et un tableau des meilleurs temps
- Pas de plaque de pression : chaque point est un carré tracé au sol (vert, jaune, rouge)
- Inventaire, mode de jeu et effets rendus à la sortie, même après un crash serveur
- Tous les textes en MiniMessage dans `messages.yml`

## Installation

1. Prendre le jar dans les [releases](https://github.com/SalksCore/parkour/releases)
2. Le mettre dans `plugins/`
3. Redémarrer le serveur

Il faut Paper (ou un fork) en 1.21 et Java 21.

## Créer un parcours

```
/pk create jump
/pk set jump start        sur le bloc de départ
/pk addcp jump            sur chaque checkpoint, dans l'ordre
/pk set jump finish       sur l'arrivée
/pk set jump spawn        optionnel, là où on renvoie les joueurs à la fin
/pk set jump board        optionnel, le tableau des meilleurs temps
/pk set jump fall         optionnel, chute sous ce Y = retour au checkpoint
/pk rename jump <gradient:#5eead4:#3b82f6>Jump Tower</gradient>
```

Chaque point est marqué par un carré au sol, sur le bloc où tu te tenais. Le joueur
démarre en entrant dans le carré de départ ou avec `/pk join jump`.

## Commandes

| Commande | Rôle |
|---|---|
| `/pk list` | Parcours disponibles, cliquables |
| `/pk join <parcours>` | Se téléporter au départ |
| `/pk checkpoint` | Retour au dernier checkpoint |
| `/pk restart` | Recommencer depuis le départ |
| `/pk leave` | Abandonner |
| `/pk top <parcours>` | Top 10 et record perso |
| `/pk create <id>` | Créer un parcours |
| `/pk set <parcours> start\|finish\|spawn\|board\|fall` | Placer un point à ta position |
| `/pk unset <parcours> spawn\|board\|fall` | Retirer un point optionnel |
| `/pk addcp <parcours>` | Ajouter un checkpoint |
| `/pk delcp <parcours> [n]` | Retirer un checkpoint (le dernier par défaut) |
| `/pk rename <parcours> <nom>` | Nom affiché, MiniMessage accepté |
| `/pk info <parcours>` | Détails du parcours |
| `/pk tp <parcours>` | Aller au départ sans lancer le chrono |
| `/pk reset <parcours> [joueur]` | Effacer les temps |
| `/pk delete <parcours>` | Supprimer le parcours |
| `/pk reload` | Recharger la config et les messages |

## Permissions

- `parkour.use` : jouer (tout le monde par défaut)
- `parkour.admin` : créer et modifier (op par défaut)

## Fichiers

- `config.yml` : couleurs des carrés, mode de jeu pendant la partie, objets, bossbar, hologrammes
- `messages.yml` : tous les textes
- `courses.yml` : les parcours
- `times.yml` : les meilleurs temps
- `states/` : inventaires des joueurs en cours de partie

## Compiler

```
mvn package
```

Le jar sort dans `target/`.

## Licence

MIT

## Mod client

Avec le mod [Parkour Companion](https://github.com/SalksCore/parkour-companion), les
joueurs ont le fantôme de leur record, les temps intermédiaires, un HUD dédié et des
checkpoints visibles à travers les murs. Le plugin détecte le mod tout seul.
