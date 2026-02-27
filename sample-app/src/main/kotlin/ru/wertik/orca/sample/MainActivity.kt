package ru.wertik.orca.sample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ru.wertik.orca.compose.Orca
import ru.wertik.orca.compose.OrcaDefaults
import ru.wertik.orca.compose.OrcaRootLayout
import ru.wertik.orca.core.OrcaMarkdownParser

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            var isDark by rememberSaveable { mutableStateOf(false) }

            MaterialTheme(
                colorScheme = if (isDark) darkColorScheme() else lightColorScheme(),
            ) {
                OrcaSampleApp(
                    isDark = isDark,
                    onToggleTheme = { isDark = !isDark },
                )
            }
        }
    }
}

private enum class SampleScreen(
    val label: String,
    val icon: ImageVector,
) {
    OVERVIEW("Overview", Icons.Default.Article),
    BLOCKS("Blocks", Icons.Default.Code),
    TABLES("Tables", Icons.Default.TableChart),
    ADVANCED("Advanced", Icons.Default.Tune),
    STREAMING("Stream", Icons.Default.PlayArrow),
    PLAYGROUND("Edit", Icons.Default.Edit),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrcaSampleApp(
    isDark: Boolean,
    onToggleTheme: () -> Unit,
) {
    val context = LocalContext.current
    val parser = remember { OrcaMarkdownParser() }
    val orcaStyle = remember(isDark) {
        if (isDark) OrcaDefaults.darkStyle() else OrcaDefaults.lightStyle()
    }

    val screens = remember { SampleScreen.entries }
    var selectedScreen by rememberSaveable { mutableIntStateOf(0) }

    val backgroundColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF121212) else Color.White,
        animationSpec = tween(300),
        label = "bg",
    )

    val onLinkClick: (String) -> Unit = { link ->
        Toast.makeText(context, "Link: $link", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Orca",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = "Compose Multiplatform Markdown",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle theme",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) Color(0xFF1E1E1E) else MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = if (isDark) Color(0xFF1E1E1E) else MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
            ) {
                screens.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        selected = selectedScreen == index,
                        onClick = { selectedScreen = index },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.label,
                            )
                        },
                        label = { Text(screen.label) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = if (isDark) Color(0xFF2D2D2D) else MaterialTheme.colorScheme.secondaryContainer,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedScreen,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(backgroundColor),
            transitionSpec = {
                fadeIn(tween(200)) togetherWith fadeOut(tween(150))
            },
            label = "screen",
        ) { screenIndex ->
            when (screens[screenIndex]) {
                SampleScreen.STREAMING -> StreamingScreen(
                    parser = parser,
                    style = orcaStyle,
                    isDark = isDark,
                    onLinkClick = onLinkClick,
                )
                SampleScreen.PLAYGROUND -> PlaygroundScreen(
                    parser = parser,
                    style = orcaStyle,
                    isDark = isDark,
                    onLinkClick = onLinkClick,
                )
                else -> {
                    val markdown = sampleMarkdown(screens[screenIndex])
                    Orca(
                        markdown = markdown,
                        parser = parser,
                        parseCacheKey = screens[screenIndex].name,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        style = orcaStyle,
                        onLinkClick = onLinkClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun StreamingScreen(
    parser: OrcaMarkdownParser,
    style: ru.wertik.orca.compose.OrcaStyle,
    isDark: Boolean,
    onLinkClick: (String) -> Unit,
) {
    val fullText = STREAMING_DEMO_MARKDOWN
    var displayedText by remember { mutableStateOf("") }
    var isStreaming by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        displayedText = ""
        isStreaming = true
        // Simulate token-by-token streaming
        val words = fullText.split(" ")
        for (i in words.indices) {
            displayedText = words.subList(0, i + 1).joinToString(" ")
            delay(40L)
        }
        isStreaming = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = if (isStreaming) "Streaming..." else "Complete",
                style = MaterialTheme.typography.labelMedium,
                color = if (isStreaming) {
                    if (isDark) Color(0xFF82B1FF) else Color(0xFF1565C0)
                } else {
                    if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                },
            )
            Text(
                text = "${displayedText.length} / ${fullText.length} chars",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }

        Orca(
            markdown = displayedText,
            parser = parser,
            parseCacheKey = "streaming-demo",
            modifier = Modifier.fillMaxSize(),
            style = style,
            onLinkClick = onLinkClick,
        )
    }
}

@Composable
private fun PlaygroundScreen(
    parser: OrcaMarkdownParser,
    style: ru.wertik.orca.compose.OrcaStyle,
    isDark: Boolean,
    onLinkClick: (String) -> Unit,
) {
    var input by rememberSaveable { mutableStateOf(PLAYGROUND_DEFAULT_MARKDOWN) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(top = 12.dp),
            label = { Text("Markdown") },
            placeholder = { Text("Type markdown here...") },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isDark) Color(0xFF82B1FF) else Color(0xFF1565C0),
                unfocusedBorderColor = if (isDark) Color(0xFF424242) else Color(0xFFD0D7DE),
                focusedContainerColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFFAFAFA),
                unfocusedContainerColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFFAFAFA),
            ),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .height(1.dp)
                .background(
                    if (isDark) Color(0xFF333333) else Color(0xFFE0E0E0),
                ),
        )

        Orca(
            markdown = input,
            parser = parser,
            parseCacheKey = "playground",
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp, bottom = 16.dp),
            style = style,
            rootLayout = OrcaRootLayout.COLUMN,
            onLinkClick = onLinkClick,
        )
    }
}

private fun sampleMarkdown(screen: SampleScreen): String {
    return when (screen) {
        SampleScreen.OVERVIEW -> OVERVIEW_MARKDOWN
        SampleScreen.BLOCKS -> BLOCKS_MARKDOWN
        SampleScreen.TABLES -> TABLES_MARKDOWN
        SampleScreen.ADVANCED -> ADVANCED_MARKDOWN
        else -> ""
    }
}

// region Markdown content

private val OVERVIEW_MARKDOWN = """
# Orca v0.8

Compose Multiplatform markdown renderer with full feature support.

## Inline formatting

**Bold**, *italic*, ~~strikethrough~~, `inline code`, and [links](https://github.com).

Combine them: ***bold italic***, **~~bold strikethrough~~**, *`italic code`*.

## Superscript & subscript

Einstein's equation: E = mc^2^

Water molecule: H~2~O

## Emoji shortcodes

:rocket: Launch ready! :fire: Hot feature :sparkles: Looking good :thumbsup:

:warning: Careful here :bug: Found one :wrench: Fixing it :white_check_mark: Done!

---

## Lists

Unordered:
- First item
- Second item with **bold**
- Third item with `code`

Ordered:
1. Step one
2. Step two
3. Step three

## Task list

- [x] Parser extensions wired
- [x] Compose rendering updated
- [x] Admonition support
- [x] Definition list support
- [ ] LaTeX math rendering
- [ ] Custom block renderers
""".trimIndent()

private val BLOCKS_MARKDOWN = """
## Admonitions

> [!NOTE]
> Orca supports GitHub-style admonitions for highlighting important information.

> [!TIP]
> Use different admonition types to convey the right level of urgency.

> [!IMPORTANT]
> Breaking changes are documented in the changelog.

> [!WARNING]
> Experimental features may change without notice.

> [!CAUTION]
> Do not use in production without thorough testing.

---

## Blockquote

> Keep architecture simple and stable first.
>
> Complexity is the enemy of reliability.

## Nested blockquote

> Outer quote
> > Inner nested quote
> > with multiple lines
>
> Back to outer

---

## Code blocks

```kotlin
fun greet(name: String) {
    println("Hello, ${'$'}name!")
}

// Syntax highlighting for keywords, strings, comments
val items = listOf("apple", "banana", "cherry")
items.forEach { item ->
    println("Fruit: ${'$'}item")
}
```

```python
def fibonacci(n: int) -> list[int]:
    ${"\"\"\""}Generate Fibonacci sequence.${"\"\"\""}
    seq = [0, 1]
    for i in range(2, n):
        seq.append(seq[-1] + seq[-2])
    return seq

print(fibonacci(10))
```

```sql
SELECT u.name, COUNT(o.id) AS order_count
FROM users u
LEFT JOIN orders o ON u.id = o.user_id
WHERE u.active = true
GROUP BY u.name
ORDER BY order_count DESC
LIMIT 10;
```

---

## HTML block

<p>This is a <b>bold</b> and <i>italic</i> HTML paragraph with a <a href="https://example.com">clickable link</a>.</p>

<blockquote>HTML blockquote with <code>inline code</code> and <mark>highlighted text</mark>.</blockquote>
""".trimIndent()

private val TABLES_MARKDOWN = """
## Tables

### Module status

| Module | Status | Platform | Docs |
|:-------|:------:|:--------:|-----:|
| **orca-core** | :white_check_mark: Ready | All | [API](https://github.com) |
| **orca-compose** | :white_check_mark: Ready | Android, Desktop, iOS | [Guide](https://github.com) |
| sample-app | :wrench: Demo | Android | `this app` |

### Feature comparison

| Feature | Supported | Notes |
|:--------|:---------:|:------|
| Bold / italic | :white_check_mark: | Standard markdown |
| Strikethrough | :white_check_mark: | GFM extension |
| Tables | :white_check_mark: | GFM extension |
| Task lists | :white_check_mark: | GFM extension |
| Footnotes | :white_check_mark: | PHP Markdown Extra |
| Admonitions | :white_check_mark: | GitHub-style |
| Definition lists | :white_check_mark: | PHP Markdown Extra |
| Abbreviations | :white_check_mark: | PHP Markdown Extra |
| Superscript / subscript | :white_check_mark: | `^super^` / `~sub~` |
| Emoji shortcodes | :white_check_mark: | `:rocket:` etc. |
| Syntax highlighting | :white_check_mark: | Regex-based |
| Front matter | :white_check_mark: | YAML / TOML |
| LaTeX math | :x: | Planned |

---

## Image

![Markdown logo](https://raw.githubusercontent.com/github/explore/main/topics/markdown/markdown.png)
""".trimIndent()

private val ADVANCED_MARKDOWN = """
## Footnotes

Orca supports footnotes[^1] for adding references and citations[^2].

[^1]: Footnotes appear at the bottom of the document. Click the number to jump here, and the arrow to go back.
[^2]: Multiple footnotes are numbered automatically.

---

## Definition lists

Orca
:   A Compose Multiplatform markdown rendering library.
:   Supports Android, iOS, Desktop, and Web targets.

Markdown
:   A lightweight markup language for creating formatted text using a plain-text editor.

Front matter
:   Metadata at the beginning of a markdown file, delimited by `---` (YAML) or `+++` (TOML).

---

## Abbreviations

*[HTML]: Hyper Text Markup Language
*[CSS]: Cascading Style Sheets
*[KMP]: Kotlin Multiplatform

Orca renders HTML content using Compose. It supports KMP targets and can style elements with CSS-like properties.

---

## Thematic breaks

Content above the break.

---

***

___

Content below the breaks.

---

## Deep nesting

- Level 1
    - Level 2 with **bold**
        - Level 3 with `code`
    - Back to level 2
- Another level 1

> Quote with a list inside:
> - Item one
> - Item two
> - Item three
>
> And a code block:
> ```kotlin
> val x = 42
> ```
""".trimIndent()

private val STREAMING_DEMO_MARKDOWN = """
# Streaming Demo

This content simulates **real-time streaming** from an LLM response.

## How it works

Orca handles streaming gracefully with a configurable debounce:

```kotlin
Orca(
    markdown = streamingText,
    parser = parser,
    streamingDebounceMs = 80L,
)
```

> [!TIP]
> The `streamingDebounceMs` parameter controls how often the markdown is re-parsed during streaming. Lower values give smoother updates but use more CPU.

## Features visible during streaming

- **Partial rendering** — content appears as it arrives
- **Incremental parsing** — only re-parses after debounce
- **Cache-friendly** — uses `parseCacheKey` to avoid duplicate work
- ~~No flickering~~ — smooth transitions between parse results

### Code example

```python
async def stream_response(prompt: str):
    async for chunk in llm.stream(prompt):
        yield chunk.text
```

The parser handles incomplete markdown gracefully — unclosed **bold, *italic*, or `code` spans are rendered as-is until the closing delimiter arrives.

:sparkles: **That's the streaming demo!** :rocket:
""".trimIndent()

private val PLAYGROUND_DEFAULT_MARKDOWN = """
# Hello, Orca! :wave:

Try editing this markdown to see **live rendering**.

## What to try

- **Bold** and *italic* text
- ~~Strikethrough~~ and `inline code`
- [Links](https://github.com)
- Emoji :rocket: :fire: :sparkles:

> Blockquotes work too!

```kotlin
val greeting = "Hello from Orca!"
println(greeting)
```

- [x] Try the playground
- [ ] Build something cool
""".trimIndent()

// endregion
