# Task 01 — Setup: deps, Tailwind v3, shadcn/ui

**Files:**
- Create: `frontend/tailwind.config.js`
- Create: `frontend/postcss.config.js`
- Create: `frontend/components.json` (gerado pelo shadcn init)
- Modify: `frontend/src/index.css`
- Modify: `frontend/package.json` (via npm install)

---

- [ ] **Step 1: Instalar dependências de runtime**

```bash
cd frontend
npm install react-router-dom react-hook-form zod @hookform/resolvers clsx tailwind-merge sonner lucide-react
```

Saída esperada: `added N packages` sem erros.

- [ ] **Step 2: Instalar Tailwind v3 + ferramentas de build**

```bash
npm install -D tailwindcss@3 postcss autoprefixer @types/node
```

- [ ] **Step 3: Gerar config do Tailwind**

```bash
npx tailwindcss init -p
```

Isso cria `tailwind.config.js` e `postcss.config.js`. Confirme que os dois existem:

```bash
ls tailwind.config.js postcss.config.js
```

- [ ] **Step 4: Substituir `tailwind.config.js`**

```js
/** @type {import('tailwindcss').Config} */
export default {
  darkMode: ['class'],
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        border: 'hsl(var(--border))',
        input: 'hsl(var(--input))',
        ring: 'hsl(var(--ring))',
        background: 'hsl(var(--background))',
        foreground: 'hsl(var(--foreground))',
        primary: {
          DEFAULT: 'hsl(var(--primary))',
          foreground: 'hsl(var(--primary-foreground))',
        },
        secondary: {
          DEFAULT: 'hsl(var(--secondary))',
          foreground: 'hsl(var(--secondary-foreground))',
        },
        destructive: {
          DEFAULT: 'hsl(var(--destructive))',
          foreground: 'hsl(var(--destructive-foreground))',
        },
        muted: {
          DEFAULT: 'hsl(var(--muted))',
          foreground: 'hsl(var(--muted-foreground))',
        },
        accent: {
          DEFAULT: 'hsl(var(--accent))',
          foreground: 'hsl(var(--accent-foreground))',
        },
        popover: {
          DEFAULT: 'hsl(var(--popover))',
          foreground: 'hsl(var(--popover-foreground))',
        },
        card: {
          DEFAULT: 'hsl(var(--card))',
          foreground: 'hsl(var(--card-foreground))',
        },
      },
      borderRadius: {
        lg: 'var(--radius)',
        md: 'calc(var(--radius) - 2px)',
        sm: 'calc(var(--radius) - 4px)',
      },
    },
  },
  plugins: [],
}
```

- [ ] **Step 5: Inicializar shadcn/ui**

```bash
npx shadcn@latest init
```

Responder às perguntas interativas:
- Style: **Default**
- Base color: **Slate**
- CSS variables: **Yes**

Isso cria `components.json` e atualiza `src/index.css` com as CSS variables + diretivas `@tailwind`.

- [ ] **Step 6: Verificar `src/index.css` gerado**

O arquivo deve conter as `@tailwind` directives e o bloco `:root { --background: ... }`. Se estiver vazio ou sem as vars, substitua por:

```css
@tailwind base;
@tailwind components;
@tailwind utilities;

@layer base {
  :root {
    --background: 0 0% 100%;
    --foreground: 222.2 84% 4.9%;
    --card: 0 0% 100%;
    --card-foreground: 222.2 84% 4.9%;
    --popover: 0 0% 100%;
    --popover-foreground: 222.2 84% 4.9%;
    --primary: 222.2 47.4% 11.2%;
    --primary-foreground: 210 40% 98%;
    --secondary: 210 40% 96.1%;
    --secondary-foreground: 222.2 47.4% 11.2%;
    --muted: 210 40% 96.1%;
    --muted-foreground: 215.4 16.3% 46.9%;
    --accent: 210 40% 96.1%;
    --accent-foreground: 222.2 47.4% 11.2%;
    --destructive: 0 84.2% 60.2%;
    --destructive-foreground: 210 40% 98%;
    --border: 214.3 31.8% 91.4%;
    --input: 214.3 31.8% 91.4%;
    --ring: 222.2 84% 4.9%;
    --radius: 0.5rem;
  }
  .dark {
    --background: 222.2 84% 4.9%;
    --foreground: 210 40% 98%;
    --card: 222.2 84% 4.9%;
    --card-foreground: 210 40% 98%;
    --popover: 222.2 84% 4.9%;
    --popover-foreground: 210 40% 98%;
    --primary: 210 40% 98%;
    --primary-foreground: 222.2 47.4% 11.2%;
    --secondary: 217.2 32.6% 17.5%;
    --secondary-foreground: 210 40% 98%;
    --muted: 217.2 32.6% 17.5%;
    --muted-foreground: 215 20.2% 65.1%;
    --accent: 217.2 32.6% 17.5%;
    --accent-foreground: 210 40% 98%;
    --destructive: 0 62.8% 30.6%;
    --destructive-foreground: 210 40% 98%;
    --border: 217.2 32.6% 17.5%;
    --input: 217.2 32.6% 17.5%;
    --ring: 212.7 26.8% 83.9%;
  }
}

@layer base {
  * {
    @apply border-border;
  }
  body {
    @apply bg-background text-foreground;
  }
}
```

- [ ] **Step 7: Adicionar componentes shadcn/ui**

```bash
npx shadcn@latest add button input label select table sheet dialog badge skeleton separator
```

Confirme que `src/components/ui/` contém os arquivos:
```bash
ls src/components/ui/
```
Esperado: `button.tsx input.tsx label.tsx select.tsx table.tsx sheet.tsx dialog.tsx badge.tsx skeleton.tsx separator.tsx`

- [ ] **Step 8: Verificar que o projeto compila**

```bash
npm run build
```

Esperado: sem erros TypeScript. Se `shadcn` criou `src/lib/utils.ts`, verifique que contém a função `cn`:

```bash
cat src/lib/utils.ts
```

Deve conter:
```typescript
import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}
```

Se não contiver, crie com esse conteúdo.

- [ ] **Step 9: Commit**

```bash
cd ..
git add frontend/package.json frontend/package-lock.json frontend/tailwind.config.js frontend/postcss.config.js frontend/components.json frontend/src/index.css frontend/src/lib/utils.ts frontend/src/components/ui/
git commit -m "feat(frontend): setup tailwind v3 e shadcn/ui"
```
