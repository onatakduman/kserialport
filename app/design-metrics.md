# KSerialPort App — Design Metrics & Specifications

## 1. Screen Dimensions & Breakpoints

| Class | Width Range | Layout | Navigation |
|-------|------------|--------|------------|
| **Compact** | < 600dp | Single column | Bottom Navigation Bar (80dp height) |
| **Medium** | 600dp – 839dp | Adaptive columns | Navigation Rail (80dp width) |
| **Expanded** | ≥ 840dp | Multi-pane | Navigation Rail (80dp width) |

### Safe Areas
- Status bar: 24dp (variable on notch devices)
- Navigation bar (system): 48dp (gesture nav) / 48dp (3-button)
- Bottom nav bar (app): 80dp
- Navigation rail width: 80dp

---

## 2. Color System

### Primary Palette (Light Theme)
| Token | Hex | Usage |
|-------|-----|-------|
| `primary` | `#059669` | Buttons, active states, links |
| `onPrimary` | `#FFFFFF` | Text on primary |
| `primaryContainer` | `#A7F3D0` | Cards, chips background |
| `onPrimaryContainer` | `#002114` | Text on primary container |
| `secondary` | `#2563EB` | TX data, secondary actions |
| `onSecondary` | `#FFFFFF` | Text on secondary |
| `secondaryContainer` | `#DBEAFE` | Secondary containers |
| `onSecondaryContainer` | `#1E3A5F` | Text on secondary container |
| `tertiary` | `#0891B2` | Tertiary accent |
| `error` | `#DC2626` | Error states, disconnect |
| `background` | `#FAFAFA` | Page background |
| `surface` | `#FFFFFF` | Card/sheet surfaces |
| `surfaceVariant` | `#F1F5F9` | Secondary surfaces |
| `outline` | `#CBD5E1` | Borders, dividers |

### Primary Palette (Dark Theme)
| Token | Hex | Usage |
|-------|-----|-------|
| `primary` | `#10B981` | Buttons, active states, links |
| `onPrimary` | `#003822` | Text on primary |
| `primaryContainer` | `#005234` | Cards, chips background |
| `onPrimaryContainer` | `#6EE7B7` | Text on primary container |
| `secondary` | `#3B82F6` | TX data, secondary actions |
| `onSecondary` | `#002B5C` | Text on secondary |
| `secondaryContainer` | `#1E3A5F` | Secondary containers |
| `onSecondaryContainer` | `#93C5FD` | Text on secondary container |
| `tertiary` | `#06B6D4` | Tertiary accent |
| `error` | `#EF4444` | Error states, disconnect |
| `background` | `#0F1419` | Page background |
| `surface` | `#1A1D21` | Card/sheet surfaces |
| `surfaceVariant` | `#23272E` | Secondary surfaces |
| `outline` | `#374151` | Borders, dividers |

### Terminal-Specific Colors
| Token | Hex | Usage |
|-------|-----|-------|
| `terminalBg` | `#0D1117` | Terminal view background |
| `terminalText` | `#E6EDF3` | Default terminal text |
| `terminalRx` | `#10B981` | Received data (green) |
| `terminalTx` | `#3B82F6` | Transmitted data (blue) |
| `terminalSystem` | `#F59E0B` | System messages (amber) |
| `terminalError` | `#EF4444` | Error messages (red) |
| `terminalTimestamp` | `#6B7280` | Timestamp text (gray) |
| `terminalCursor` | `#10B981` | Cursor/blink indicator |

### Status Colors
| Token | Hex | Usage |
|-------|-----|-------|
| `connected` | `#10B981` | Connected indicator |
| `disconnected` | `#EF4444` | Disconnected indicator |
| `connecting` | `#F59E0B` | Connecting/pending |
| `idle` | `#6B7280` | Idle/unknown state |

---

## 3. Typography Scale

| Style | Font | Size | Weight | Line Height | Letter Spacing | Usage |
|-------|------|------|--------|-------------|----------------|-------|
| `displayLarge` | System Default | 57sp | 400 | 64sp | -0.25sp | — |
| `displayMedium` | System Default | 45sp | 400 | 52sp | 0sp | — |
| `displaySmall` | System Default | 36sp | 400 | 44sp | 0sp | — |
| `headlineLarge` | System Default | 32sp | 400 | 40sp | 0sp | Screen titles |
| `headlineMedium` | System Default | 28sp | 400 | 36sp | 0sp | Section headers |
| `headlineSmall` | System Default | 24sp | 400 | 32sp | 0sp | Card titles |
| `titleLarge` | System Default | 22sp | 500 | 28sp | 0sp | Top app bar |
| `titleMedium` | System Default | 16sp | 500 | 24sp | 0.15sp | List item primary |
| `titleSmall` | System Default | 14sp | 500 | 20sp | 0.1sp | Card subtitles |
| `bodyLarge` | System Default | 16sp | 400 | 24sp | 0.5sp | Body text |
| `bodyMedium` | System Default | 14sp | 400 | 20sp | 0.25sp | Secondary text |
| `bodySmall` | System Default | 12sp | 400 | 16sp | 0.4sp | Caption text |
| `labelLarge` | System Default | 14sp | 500 | 20sp | 0.1sp | Buttons, tabs |
| `labelMedium` | System Default | 12sp | 500 | 16sp | 0.5sp | Nav labels, chips |
| `labelSmall` | System Default | 11sp | 500 | 16sp | 0.5sp | Badges |
| `terminalMono` | JetBrains Mono / Monospace | 14sp | 400 | 20sp | 0sp | Terminal output |

---

## 4. Spacing System (4dp Grid)

| Token | Value | Usage |
|-------|-------|-------|
| `xs` | 4dp | Inline spacing, icon padding |
| `sm` | 8dp | Chip spacing, compact gaps |
| `md` | 12dp | Card internal padding |
| `base` | 16dp | Standard padding, list item padding |
| `lg` | 20dp | Section spacing |
| `xl` | 24dp | Screen padding (horizontal) |
| `2xl` | 32dp | Section gaps |
| `3xl` | 48dp | Large section breaks |
| `4xl` | 64dp | Top-level spacing |

### Screen Margins
- Compact: 16dp horizontal
- Medium: 24dp horizontal
- Expanded: 24dp horizontal (content max-width: 840dp centered)

---

## 5. Component Specifications

### Navigation Bar (Bottom — Compact)
- Height: 80dp
- Icon size: 24dp
- Label: `labelMedium` (12sp)
- Active indicator: 64dp x 32dp, pill shape, primaryContainer fill
- Items: 4 (Home, Terminal, Scanner, Settings)
- Elevation: Level 2 (3dp)

### Navigation Rail (Medium/Expanded)
- Width: 80dp
- Icon size: 24dp
- Label: `labelMedium` (12sp)
- Active indicator: 56dp x 32dp pill
- Top padding: 8dp from top
- FAB slot: optional (not used)

### Top App Bar
- Height: 64dp
- Title: `titleLarge` (22sp, 500 weight)
- Leading icon: 48dp touch target, 24dp icon
- Trailing actions: 48dp touch target each
- Elevation: Level 0 (scroll) / Level 2 (lifted)

### Cards (ElevatedCard)
- Corner radius: 12dp
- Elevation: Level 1 (1dp)
- Internal padding: 16dp
- Min height: 56dp
- Gap between cards: 12dp

### Buttons
| Type | Height | Corner | Padding H | Font |
|------|--------|--------|-----------|------|
| Filled | 40dp | 20dp (full) | 24dp | `labelLarge` |
| FilledTonal | 40dp | 20dp | 24dp | `labelLarge` |
| Outlined | 40dp | 20dp | 24dp | `labelLarge` |
| Text | 40dp | 20dp | 12dp | `labelLarge` |
| FAB | 56dp | 16dp | — | — |
| Small FAB | 40dp | 12dp | — | — |
| Extended FAB | 56dp | 16dp | 16dp | `labelLarge` |

### Text Fields (OutlinedTextField)
- Height: 56dp
- Corner radius: 4dp
- Label: `bodySmall` (12sp, floated)
- Input text: `bodyLarge` (16sp)
- Padding: 16dp horizontal
- For terminal input: dark variant with monospace font

### Chips (FilterChip / AssistChip)
- Height: 32dp
- Corner radius: 8dp
- Label: `labelLarge` (14sp)
- Icon: 18dp
- Horizontal padding: 16dp
- Gap between chips: 8dp

### Connection Status Indicator
- Dot size: 12dp (Home card), 8dp (Top bar)
- Pulse animation: 1.5s ease-in-out, scale 1.0 → 1.4
- Connected: green dot with pulse
- Disconnected: red dot, static
- Connecting: amber dot with rotate animation

### Terminal View
- Background: `terminalBg` (#0D1117)
- Corner radius: 12dp (top), 0dp (if full-width)
- Text: `terminalMono` (14sp, monospace)
- Line padding: 4dp vertical
- Timestamp width: 72dp fixed
- Type badge: [RX] [TX] [SYS] [ERR] — 8dp horizontal padding
- Auto-scroll: smooth scroll to bottom on new entry
- Max visible lines: unlimited (LazyColumn virtualized)

### Stats Bar
- Height: 48dp
- Background: surfaceVariant
- Stat item: icon (16dp) + label (`labelSmall`) + value (`titleSmall`)
- Gap between stats: 16dp
- Display mode chips: FilterChip, 8dp gap

### Input Bar (Terminal)
- Height: auto (min 56dp)
- Macro row height: 40dp
- Macro chips: AssistChip, 8dp gap, horizontal scroll
- Input field: fills remaining width
- Send FAB: 40dp (SmallFAB), positioned at end
- Background: surface

### Device Card (Scanner)
- Type: OutlinedCard
- Corner radius: 12dp
- Padding: 16dp
- Leading: icon (40dp container, 24dp icon)
- Title: device name (`titleMedium`)
- Subtitle: full path (`bodySmall`)
- Trailing: driver type chip (`labelSmall`)
- Divider: 0.5dp, outline color
- Tap: ripple effect, navigate to Terminal

### Settings Items
- Type: ListItem inside Card sections
- Section header: `titleSmall`, primary color, 16dp padding top
- Item height: 56dp (single-line) / 72dp (two-line)
- Trailing: Switch (52dp x 32dp) / Dropdown / Chevron
- Divider: 0.5dp between items within a section

---

## 6. Icon Specifications

### Navigation Icons (Material Symbols Outlined, 24dp)
| Screen | Icon | Outlined | Filled (active) |
|--------|------|----------|-----------------|
| Home | `Home` | `home` | `home` (filled) |
| Terminal | `Terminal` | `terminal` | `terminal` (filled) |
| Scanner | `Sensors` | `sensors` | `sensors` (filled) |
| Settings | `Settings` | `settings` | `settings` (filled) |

### Action Icons (24dp)
- Connect: `link`
- Disconnect: `link_off`
- Send: `send`
- Scan: `search` or `radar`
- Clear: `delete_sweep`
- Export: `file_download`
- Add macro: `add`
- Theme toggle: `dark_mode` / `light_mode`
- About: `info`
- Upgrade/Pro: `workspace_premium`

### Status Icons (16dp inline)
- RX: `arrow_downward` (green)
- TX: `arrow_upward` (blue)
- Error: `error` (red)
- Baud rate: `speed`
- Device: `usb`

---

## 7. Ad Placement Dimensions

### Banner Ad
- Size: Adaptive Banner (320dp x 50dp standard, flexible width)
- Position: Bottom of content area, above navigation bar
- Margin top: 0dp (flush with content)
- Background: transparent (blends with surface)
- Screens: Home, Terminal, Scanner

### Interstitial Ad
- Full screen overlay
- Trigger: After first successful connection per session
- Cooldown: 180 seconds between shows
- Skip: Available after 5 seconds

---

## 8. Responsive Layout Rules

### Compact (Phone Portrait, < 600dp)
```
┌──────────────────────────────┐
│ Top App Bar (64dp)           │
├──────────────────────────────┤
│                              │
│ Screen Content               │
│ (padding: 16dp horizontal)  │
│                              │
├──────────────────────────────┤
│ Ad Banner (50dp)             │
├──────────────────────────────┤
│ Bottom Nav Bar (80dp)        │
└──────────────────────────────┘
```

### Medium (Tablet Portrait / Phone Landscape, 600-839dp)
```
┌────────┬─────────────────────────┐
│Nav Rail│ Top App Bar (64dp)      │
│ (80dp) ├─────────────────────────┤
│        │                         │
│  Home  │ Screen Content          │
│Terminal│ (padding: 24dp)         │
│Scanner │                         │
│Settings│                         │
│        ├─────────────────────────┤
│        │ Ad Banner (50dp)        │
└────────┴─────────────────────────┘
```

### Expanded (Tablet Landscape, ≥ 840dp)
```
┌────────┬──────────────────┬──────────────────┐
│Nav Rail│ Primary Pane     │ Detail Pane      │
│ (80dp) │ (flex, max 480dp)│ (flex)           │
│        │                  │                  │
│  Home  │ e.g., Device List│ e.g., Terminal   │
│Terminal│                  │                  │
│Scanner │                  │                  │
│Settings│                  │                  │
│        ├──────────────────┴──────────────────┤
│        │ Ad Banner (50dp, full width)        │
└────────┴─────────────────────────────────────┘
```

### Terminal Screen — Compact Layout Detail
```
┌──────────────────────────────┐
│ App Bar: "Terminal" + status │  64dp
├──────────────────────────────┤
│ Stats: RX | TX | ERR | Mode │  48dp
├──────────────────────────────┤
│                              │
│ Terminal View (LazyColumn)   │  flex
│ [10:23:45] [RX] data...     │
│ [10:23:46] [TX] AT+RST      │
│                              │
├──────────────────────────────┤
│ Macros: [AT] [RST] [PING]   │  40dp
├──────────────────────────────┤
│ [Input field...        ] [>] │  56dp
├──────────────────────────────┤
│ Ad Banner                    │  50dp
├──────────────────────────────┤
│ Bottom Nav                   │  80dp
└──────────────────────────────┘
```

### Home Screen — Compact Layout Detail
```
┌──────────────────────────────┐
│ App Bar: "KSerialPort"       │  64dp
├──────────────────────────────┤
│ ┌──────────────────────────┐ │
│ │ Connection Status Card   │ │  ~120dp
│ │  ● Connected / ○ Idle    │ │
│ │  /dev/ttyUSB0 @ 115200   │ │
│ └──────────────────────────┘ │
│                              │
│ ┌──────────────────────────┐ │
│ │ Quick Connect            │ │  ~80dp
│ │  Last: /dev/ttyUSB0      │ │
│ │  [Connect]               │ │
│ └──────────────────────────┘ │
│                              │
│ Recent Connections           │
│ ┌──────────────────────────┐ │
│ │ ttyUSB0 · 115200 · 2m   │ │  56dp each
│ ├──────────────────────────┤ │
│ │ ttyACM0 · 9600 · 1h     │ │
│ └──────────────────────────┘ │
├──────────────────────────────┤
│ Ad Banner                    │  50dp
├──────────────────────────────┤
│ Bottom Nav                   │  80dp
└──────────────────────────────┘
```

---

## 9. Animation Specifications

| Animation | Duration | Easing | Property |
|-----------|----------|--------|----------|
| Screen transition (forward) | 300ms | EaseInOut | Slide left + fade |
| Screen transition (back) | 250ms | EaseOut | Slide right + fade |
| Connection pulse | 1500ms | EaseInOut | Scale 1.0 → 1.4, opacity 1.0 → 0.3, loop |
| Card press | 100ms | EaseIn | Scale 1.0 → 0.98 |
| FAB press | 100ms | EaseIn | Scale 1.0 → 0.95 |
| Stats counter | 200ms | LinearOutSlowIn | Number increment |
| Scan progress | Indeterminate | — | LinearProgressIndicator |
| Theme switch | 400ms | EaseInOut | Color crossfade |
| Chip select | 200ms | EaseInOut | Background color + checkmark |

---

## 10. Elevation System (Material3 Tonal)

| Level | Elevation | Tonal Overlay | Usage |
|-------|-----------|---------------|-------|
| 0 | 0dp | 0% | Background surfaces |
| 1 | 1dp | 5% | Cards, navigation rail |
| 2 | 3dp | 8% | Top app bar (scrolled), bottom nav |
| 3 | 6dp | 11% | FABs, bottom sheets |
| 4 | 8dp | 12% | Dialogs |
| 5 | 12dp | 14% | Modals |

---

## 11. Pro Feature Components

### Pro Badge
- Container: `tertiaryContainer` color, rounded 4dp
- Padding: 8dp horizontal, 2dp vertical
- Icon: `lock`, 12dp, `onTertiaryContainer` color
- Text: "PRO", 10sp, bold, `onTertiaryContainer` color
- Usage: Inline next to gated feature section titles

### Macro Editor (Settings Screen)
- Section title "Macros" with Pro badge (if free) or "+ Add" button (if Pro)
- Each macro: `ListItem` inside `Card`
  - Headline: macro text (`bodyLarge`)
  - Trailing: Edit (20dp icon) + Delete (20dp icon) buttons (Pro only)
  - Divider: `HorizontalDivider` between items
- Add/Edit Dialog: `AlertDialog`
  - Title: "Add Macro" / "Edit Macro"
  - Body: `OutlinedTextField` for command text
  - Actions: "Save" + "Cancel" `TextButton`

### Connection Profile Card (Settings Screen)
- Section title "Profiles" with "Save Current" button (Pro)
- Empty state: `ListItem` with hint text
- Profile item: `ListItem` inside `Card`
  - Headline: profile name (`titleMedium`)
  - Supporting: `path @ baudRate` (`bodySmall`)
  - Trailing: "Load" `TextButton` + Delete icon
- Save Dialog: `AlertDialog`
  - Body: `OutlinedTextField` for profile name
  - Actions: "Save" + "Cancel"

### Auto-Reply Rule Card (Settings Screen)
- Section title "Auto-Reply" with "+ Add Rule" button (Pro)
- Rule item: `ListItem` inside `Card`
  - Headline: `"trigger" → "response"` format
  - Trailing: `Switch` (enable/disable) + Delete icon
- Add/Edit Dialog: `AlertDialog`
  - Body: 2x `OutlinedTextField` (trigger + response)
  - Actions: "Save" + "Cancel"

### Periodic Macro (Terminal Screen)
- Location: End of macro chips row
- Active state: `AssistChip` with `stop` icon (red) + "Stop" label
- Inactive state: `AssistChip` with `repeat` icon + "Repeat" label
- Free state: Disabled `AssistChip` with Pro badge
- Config Dialog: `AlertDialog`
  - Body: Command `OutlinedTextField` + Interval (ms) `OutlinedTextField` (number keyboard)
  - Min interval: 50ms
  - Actions: "Start" + "Cancel"

### Log Export Button (Terminal Screen)
- Location: Top app bar actions (before clear button)
- Icon: `file_download`, 24dp
- Visible only for Pro users
- Disabled when logs are empty
- Action: Write to cache → Share sheet via `FileProvider`

### Settings Screen — Compact Layout with Pro Features
```
┌──────────────────────────────┐
│ App Bar: "Settings"          │  64dp
├──────────────────────────────┤
│ Serial Port                  │
│ ┌──────────────────────────┐ │
│ │ Baud Rate, Bits, Parity  │ │
│ │ Line Ending              │ │
│ └──────────────────────────┘ │
│                              │
│ Macros [PRO] [+ Add]        │
│ ┌──────────────────────────┐ │
│ │ AT            [✎] [🗑]  │ │
│ ├──────────────────────────┤ │
│ │ AT+RST        [✎] [🗑]  │ │
│ └──────────────────────────┘ │
│                              │
│ Profiles [PRO] [Save Current]│
│ ┌──────────────────────────┐ │
│ │ Arduino · /dev/ttyUSB0   │ │
│ │         [Load] [🗑]      │ │
│ └──────────────────────────┘ │
│                              │
│ Auto-Reply [PRO] [+ Add Rule]│
│ ┌──────────────────────────┐ │
│ │ "PING" → "PONG"  [⬤][🗑]│ │
│ └──────────────────────────┘ │
│                              │
│ Display                      │
│ ┌──────────────────────────┐ │
│ │ Auto-scroll         [⬤] │ │
│ │ Show timestamps     [⬤] │ │
│ └──────────────────────────┘ │
│                              │
│ Appearance                   │
│ ┌──────────────────────────┐ │
│ │ [System|Light|Dark]      │ │
│ │ Dynamic colors      [⬤] │ │
│ └──────────────────────────┘ │
│                              │
│ ┌──────────────────────────┐ │
│ │ ⭐ Upgrade to Pro    [>] │ │
│ └──────────────────────────┘ │
│                              │
│ ┌──────────────────────────┐ │
│ │ ℹ About              [>] │ │
│ └──────────────────────────┘ │
├──────────────────────────────┤
│ Bottom Nav                   │  80dp
└──────────────────────────────┘
```
