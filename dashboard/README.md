# Feature Management UI

A modern, responsive web application built with Vue 3 and Vite for managing feature flags and environment-specific configurations. The application features a clean, professional design with a focus on usability and aesthetics.

## Features

- 🎯 **Feature Management**: Create, toggle, and manage feature flags across different environments (Dashboard, Features, Environments).
- 🎨 **Modern Interface**: Beautiful UI styling with responsive design and smooth micro-animations.
- 🌓 **Theme Support**: Built-in support for multiple themes (e.g., Light and Dark modes) with seamless switching.
- 📱 **Responsive Layout**: Fully responsive application with a collapsible sidebar and mobile-friendly drawer menu.
- ⚡ **Lightning Fast**: Built on Vite for incredibly fast development server startup and Hot Module Replacement (HMR).

## Tech Stack

- **Framework**: [Vue 3](https://vuejs.org/) (Composition API & `<script setup>`)
- **Build Tool**: [Vite](https://vitejs.dev/)
- **Language**: [TypeScript](https://www.typescriptlang.org/)
- **Styling**: [Tailwind CSS v4](https://tailwindcss.com/)
- **State Management**: [Pinia](https://pinia.vuejs.org/)
- **Routing**: [Vue Router](https://router.vuejs.org/)
- **Icons**: [Lucide Vue Next](https://lucide.dev/)

## Recommended IDE Setup

[VS Code](https://code.visualstudio.com/) + [Vue (Official)](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (and disable Vetur).

## Getting Started

### Prerequisites

Ensure you have [Node.js](https://nodejs.org/) (v20.19.0+ or >=22.12.0) installed on your system.

### Project Setup

1. Clone the repository and navigate into the project directory.

2. **Environment Configuration**: 
   For manual setup, copy `.env.example` to `.env` and configure your API base URL:
   ```sh
   cp .env.example .env
   ```

3. Install the necessary dependencies:
   ```sh
   npm install
   ```

### Compile and Hot-Reload for Development

Start the application in development mode with HMR:
```sh
npm run dev
```

### Type-Check, Compile and Minify for Production

Build the optimized, production-ready bundle:
```sh
npm run build
```

### Linting & Formatting

Lint the codebase using ESLint and Oxlint:
```sh
npm run lint
```

Format source code:
```sh
npm run format
```
