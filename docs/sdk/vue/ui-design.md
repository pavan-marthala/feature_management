# UI Design System (Glassmorphism)

The Feature Management Dashboard uses a premium **Glassmorphism** design system, combining transparency, blur, and vibrant colors to create a modern and visually appealing experience.

## 🎨 Color Palette

-   **Primary (Indigo)**: `#6366F1` (Action buttons, primary highlights).
-   **Surface (Glass)**: `rgba(255, 255, 255, 0.05)` (Backgrounds of cards and sidebars).
-   **Accent (Teal)**: `#14B8A6` (Active feature status).
-   **Warning (Rose)**: `#F43F5E` (Disabled feature status).

## 🏙 Typography

-   **Headings**: *Inter* (Semi-bold / Bold).
-   **Body**: *Roboto* (Regular).
-   **Monospace**: *JetBrains Mono* (Key codes and JSON previews).

---

## ✨ CSS Utilities (Tailwind Config)

The following Tailwind CSS utilities are used to achieve the glass effect:

```css
.glass-panel {
  background: rgba(255, 255, 255, 0.05); /* Semi-transparent background */
  backdrop-filter: blur(12px);          /* Core glass effect */
  border: 1px solid rgba(255, 255, 255, 0.1); /* Subtle border highlight */
  border-radius: 1rem;                  /* Rounded corners */
}

.glass-card:hover {
  background: rgba(255, 255, 255, 0.08);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
  transition: all 0.3s ease-in-out;
}
```

## 📐 Layout System

-   **Grid**: 12-column responsive grid system.
-   **Padding**: Consistent `p-4` (1rem) or `p-6` (1.5rem) spacing.
-   **Icons**: *Lucide Vue Next* for lightweight, stroke-based iconography.

## 🎞 Micro-Animations

-   **Toggle Slide**: Smooth 300ms transition for feature status changes.
-   **Hover Lift**: Subtle `scale-105` on feature cards during interaction.
-   **Skeleton Loaders**: Pulsing glass panels for API loading states.
