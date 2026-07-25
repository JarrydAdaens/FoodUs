# ACME Food App

> **ACME Food App** is Jarryd Adaens's personal fork of
> [Food You](https://github.com/maksimowiczm/FoodYou) (GPL-3.0) by Mateusz Maksimowicz. It tailors
> the app for private household use: recovered historical data, AI-assisted logging, and
> quality-of-life changes, all as additive overlays that stay mergeable with upstream. Project
> direction lives in [context/design.md](context/design.md) (with its Milestones Index),
> [context/milestones/](context/milestones/), and [context/backlog/](context/backlog/); agent
> workflow rules live in [AGENTS.md](AGENTS.md) and [AGENTIC_RAILS_README.MD](AGENTIC_RAILS_README.MD).

[![Build](https://github.com/JarrydAdaens/FoodYou/actions/workflows/ci.yml/badge.svg?branch=jarryd%2Fmain)](https://github.com/JarrydAdaens/FoodYou/actions/workflows/ci.yml?query=branch%3Ajarryd%2Fmain)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/JarrydAdaens/FoodYou?color=black&label=Release&logo=github)](https://github.com/JarrydAdaens/FoodYou/releases/latest/)
[![GitHub Repo stars](https://img.shields.io/github/stars/JarrydAdaens/FoodYou?style=flat&logo=github&color=%23f8e444)](https://github.com/JarrydAdaens/FoodYou/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/JarrydAdaens/FoodYou?style=flat&logo=github)](https://github.com/JarrydAdaens/FoodYou/forks)

> The Build badge tracks this fork's own CI ([.github/workflows/ci.yml](.github/workflows/ci.yml))
> on `jarryd/main`, and the Release badge tracks this fork's GitHub releases. They render live
> results once GitHub Actions is enabled on the fork, the default branch is switched to
> `jarryd/main`, and a push triggers the first CI run; the Release badge resolves after the first
> fork release is published.

<div align="center">
    <img src="./metadata/en-US/images/featureGraphic.png" alt="Feature Graphic" />
</div>

ACME Food App is a free, open-source, and privacy-focused food diary and nutrition tracker built
using [Material Design](https://m3.material.io/) principles, based on
[Food You](https://github.com/maksimowiczm/FoodYou).

## Installation

This fork is not distributed through any public store. It is built locally and side-loaded onto
the two household devices via adb; a repeatable signing/update mechanism is a Milestone 1 story.
For the original app, see [Food You on F-Droid](https://f-droid.org/packages/com.maksimowiczm.foodyou)
or [its GitHub releases](https://github.com/maksimowiczm/FoodYou/releases).

## ✨ Features

<br>

<div align="center">
  <img src="metadata/en-US/images/phoneScreenshots/1.png" width="23%" alt="Modular Home Screen"/>
  <img src="metadata/en-US/images/phoneScreenshots/2.png" width="23%" alt="Comprehensive Food Databases"/>
  <img src="metadata/en-US/images/phoneScreenshots/3.png" width="23%" alt="Full Nutrition Tracking"/>
  <img src="metadata/en-US/images/phoneScreenshots/4.png" width="23%" alt="Recipe Creation"/>
</div>

<br>

- 🔒 **Privacy First** – No account required, all data stored locally on your device
- 🧩 **Modular Home Screen** – Customize your home view with functional cards that suit your habits
- 📚 **Comprehensive Food Databases** – Seamlessly integrates Open Food Facts, USDA FoodData Central,
  and Swiss Food Composition Database
- 🧪 **Full Nutrition Tracking** – Set and track personalized nutrition targets, monitoring not only
  calories and macros but also vitamins, minerals, and other essential nutrients
- 🍲 **Recipe Creation** – Create custom recipes by combining foods, with nutrition calculated
  instantly
- 🎨 **Material You Design** – Adaptive theming and modern UI

Planned fork additions (see [context/design.md](context/design.md)): recovered MyFitnessPal /
Lose It history via an owner-controlled master data format, AI-assisted logging, and
lower-friction logging ergonomics.

## 🤝 Contributing

This is a personal fork maintained for private household use, and it is not seeking code
contributions. Issues and ideas for the fork are tracked in
[this repository's issues](https://github.com/JarrydAdaens/FoodYou/issues) and the project's
[context tiers](context/design.md).

To contribute to the original app — translations, feature requests, or bug reports — please go
[upstream to Food You](https://github.com/maksimowiczm/FoodYou#-contributing).

## 💡 Credits

- [Food You](https://github.com/maksimowiczm/FoodYou) by Mateusz Maksimowicz — the upstream
  project this fork is built on. If you find this useful, support the original author on
  [Ko-fi](https://ko-fi.com/maksimowiczm).
- [ReadYou](https://github.com/Ashinch/ReadYou) — upstream's acknowledged inspiration 🙃
- [Icons8](https://icons8.com) — sushi icon 🍣

## ✉️ Contact

- **Fork:** Jarryd Adaens — [jarryd.adaens@outlook.com.au](mailto:jarryd.adaens@outlook.com.au)
- **Upstream:** [maksimowicz.dev@gmail.com](mailto:maksimowicz.dev@gmail.com?subject=Food%20You) ·
  [Discord](https://discord.gg/MuF6VZjufn)

## 📜 License

This fork remains under the GNU General Public License v3.0, as required by the upstream license.

```
Copyright (C) 2024-2026 Mateusz Maksimowicz
Modifications Copyright (C) 2026 Jarryd Adaens

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
```
