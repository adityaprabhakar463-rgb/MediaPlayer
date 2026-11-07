package com.celestial.player.scene.host

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.celestial.player.scene.camera.OrbitCamera
import com.celestial.player.scene.math.Matrix4
import com.celestial.player.scene.math.ProjectedPoint
import com.celestial.player.scene.math.Vector3
import com.celestial.player.scene.model.CentralStar3D
import com.celestial.player.scene.model.Planet3D
import com.celestial.player.scene.model.Starfield3D
import com.celestial.player.ui.components.glass.CelestialGlassSurface
import com.celestial.player.ui.components.glass.GlassIconButton
import com.celestial.player.ui.navigation.DestinationRegistry
import com.celestial.player.ui.navigation.NavTarget
import com.celestial.player.ui.theme.CelestialColors
import com.celestial.player.ui.theme.CelestialTheme
import kotlin.math.cos
import kotlin.math.sin

private val LabelHorizontalPadding = 10.dp
private val LabelVerticalPadding = 5.dp
private val LabelGapAbovePlanet = 10.dp
private val LabelSideMargin = 6.dp
private val LabelCollisionGap = 4.dp
private val LabelTopInset = 104.dp
private val LabelBottomInset = 108.dp
private const val MinLabelTouchTargetDp = 48f
private const val MaxVisibleLabels = 7

private data class PlanetLabelLayout(
    val planet: Planet3D,
    val rect: Rect,
    val textLayout: TextLayoutResult,
    val baseWidth: Float,
    val baseHeight: Float,
    val alpha: Float,
    val scale: Float,
    val selected: Boolean,
)

/**
 * Camera, orbit and star state that survives navigation into a destination and back, so returning
 * to the universe restores the previous orientation and planet positions exactly.
 */
class UniverseSceneState {
    val camera = OrbitCamera()
    val starfield = Starfield3D()
    val centralStar = CentralStar3D()
    val planets = Planet3D.defaultPlanets()
    var elapsedTimeSec by mutableFloatStateOf(0f)
}

@Composable
fun CelestialUniverseScene(
    onSelectPlanet: (NavTarget) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    returningFromTarget: NavTarget? = null,
    sceneState: UniverseSceneState = remember { UniverseSceneState() },
) {
    val camera = sceneState.camera
    val starfield = sceneState.starfield
    val centralStar = sceneState.centralStar
    val planets = sceneState.planets
    val reducedMotion = CelestialTheme.effects.reducedMotion
    val colors = CelestialTheme.colors
    val textMeasurer = rememberTextMeasurer()
    val labelTextStyle = MaterialTheme.typography.labelMedium
    val haptic = LocalHapticFeedback.current

    val returnPlanet = remember(returningFromTarget) {
        returningFromTarget?.let { Planet3D.findPlanetForTarget(it) }
    }

    var selectedPlanet by remember { mutableStateOf<Planet3D?>(null) }
    var destinationsOpen by remember { mutableStateOf(false) }
    var isApproaching by remember { mutableStateOf(false) }
    var approachProgress by remember { mutableFloatStateOf(0f) }
    var approachStartEye by remember { mutableStateOf(Vector3.ZERO) }
    var approachTargetEye by remember { mutableStateOf(Vector3.ZERO) }

    var isPullingBack by remember { mutableStateOf(returnPlanet != null) }
    var pullbackProgress by remember { mutableFloatStateOf(0f) }

    var totalDragDistance by remember { mutableFloatStateOf(0f) }

    fun beginApproach(targetPlanet: Planet3D) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        selectedPlanet = targetPlanet
        isApproaching = true
        approachProgress = 0f
        approachStartEye = camera.calculateEye()
        val planetPos = targetPlanet.calculatePosition(sceneState.elapsedTimeSec)
        val viewDir = (camera.calculateEye() - planetPos).normalized()
        approachTargetEye = planetPos + viewDir * (targetPlanet.radius * 3.2f)
    }

    LaunchedEffect(reducedMotion) {
        var lastTime = 0L
        while (true) {
            withFrameNanos { nanos ->
                if (lastTime != 0L) {
                    val deltaSec = (nanos - lastTime) / 1_000_000_000f
                    if (!reducedMotion) {
                        sceneState.elapsedTimeSec += deltaSec
                    }
                    camera.updateInertia()
                    if (isApproaching) {
                        approachProgress = (approachProgress + deltaSec * 1.8f).coerceAtMost(1f)
                        val eye = Vector3.lerp(approachStartEye, approachTargetEye, approachProgress)
                        val targetPlanet = selectedPlanet
                        if (targetPlanet != null) {
                            camera.target = Vector3.lerp(
                                Vector3.ZERO,
                                targetPlanet.calculatePosition(sceneState.elapsedTimeSec),
                                approachProgress,
                            )
                        }
                        camera.distance = eye.distanceTo(camera.target)
                        if (approachProgress >= 1f) {
                            isApproaching = false
                            selectedPlanet?.let { onSelectPlanet(it.target) }
                        }
                    } else if (isPullingBack && returnPlanet != null) {
                        pullbackProgress = (pullbackProgress + deltaSec * 1.6f).coerceAtMost(1f)
                        val planetPos = returnPlanet.calculatePosition(sceneState.elapsedTimeSec)
                        camera.target = Vector3.lerp(planetPos, Vector3.ZERO, pullbackProgress)
                        val startDist = returnPlanet.radius * 3.2f
                        camera.distance = startDist + (14f - startDist) * pullbackProgress
                        if (pullbackProgress >= 1f) {
                            isPullingBack = false
                        }
                    }
                }
                lastTime = nanos
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                contentDescription = "3D Celestial Solar System Universe"
                customActions = DestinationRegistry.entries.map { entry ->
                    CustomAccessibilityAction("Open ${entry.label}") {
                        onSelectPlanet(entry.target)
                        true
                    }
                }
            },
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures(
                        onGesture = { _, pan, zoom, _ ->
                            val dragAmount = kotlin.math.sqrt(pan.x * pan.x + pan.y * pan.y)
                            totalDragDistance += dragAmount
                            if (zoom != 1f) {
                                camera.distance = (camera.distance / zoom).coerceIn(4f, 30f)
                            }
                            if (pan != Offset.Zero && !isApproaching) {
                                camera.velocityYaw = -pan.x * 0.25f
                                camera.velocityPitch = pan.y * 0.25f
                                camera.yawDeg -= pan.x * 0.25f
                                camera.pitchDeg = (camera.pitchDeg + pan.y * 0.25f).coerceIn(-85f, 85f)
                            }
                        },
                    )
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val changes = event.changes
                            if (changes.size == 1) {
                                val change = changes[0]
                                if (change.pressed) {
                                    totalDragDistance = 0f
                                } else if (change.previousPressed) {
                                    if (totalDragDistance < 15f && !isApproaching) {
                                        val touchX = change.position.x
                                        val touchY = change.position.y
                                        val viewportW = size.width.toFloat()
                                        val viewportH = size.height.toFloat()

                                        val ray = camera.unprojectRay(touchX, touchY, viewportW, viewportH)
                                        var closestPlanet: Planet3D? = null
                                        var closestDist = Float.MAX_VALUE

                                        planets.forEach { planet ->
                                            val planetPos = planet.calculatePosition(sceneState.elapsedTimeSec)
                                            val t = ray.intersectsSphere(planetPos, planet.radius * 1.5f)
                                            if (t != null && t < closestDist) {
                                                closestDist = t
                                                closestPlanet = planet
                                            }
                                        }

                                        var targetPlanet = closestPlanet
                                        if (targetPlanet == null) {
                                            val viewProj = camera.getProjectionMatrix(viewportW / viewportH) *
                                                camera.getViewMatrix()
                                            val labels = computePlanetLabels(
                                                planets = planets,
                                                viewProj = viewProj,
                                                viewport = Size(viewportW, viewportH),
                                                timeSec = sceneState.elapsedTimeSec,
                                                textMeasurer = textMeasurer,
                                                textStyle = labelTextStyle,
                                                density = this,
                                                selectedTarget = selectedPlanet?.target,
                                                topInsetPx = LabelTopInset.toPx(),
                                                bottomInsetPx = LabelBottomInset.toPx(),
                                                obstacle = centralStarObstacle(viewProj, viewportW, viewportH, centralStar),
                                            )
                                            val minTouch = MinLabelTouchTargetDp.dp.toPx()
                                            val touch = Offset(touchX, touchY)
                                            targetPlanet = labels.firstOrNull { label ->
                                                expandToMinimumTouchTarget(label.rect, minTouch).contains(touch)
                                            }?.planet
                                        }

                                        if (targetPlanet != null) {
                                            beginApproach(targetPlanet)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
            onDraw = {
                val viewportW = size.width
                val viewportH = size.height
                val aspect = viewportW / viewportH

                val viewMatrix = camera.getViewMatrix()
                val projMatrix = camera.getProjectionMatrix(aspect)
                val viewProj = projMatrix * viewMatrix

                drawRect(color = Color(0xFF030308))

                drawStarfield(starfield, viewProj, viewportW, viewportH, sceneState.elapsedTimeSec, reducedMotion)

                drawCentralStar(centralStar, viewProj, viewportW, viewportH, sceneState.elapsedTimeSec)

                drawOrbitalTracks(planets, viewProj, viewportW, viewportH)

                val sortedPlanets = planets.map { planet ->
                    val pos = planet.calculatePosition(sceneState.elapsedTimeSec)
                    val proj = viewProj.project(pos, viewportW, viewportH)
                    Triple(planet, pos, proj)
                }.sortedByDescending { it.third.w }

                sortedPlanets.forEach { (planet, pos, proj) ->
                    if (!proj.isBehind) {
                        drawPlanet3D(
                            planet = planet,
                            position = pos,
                            projected = proj,
                            centralStarPos = centralStar.position,
                            isSelected = planet == selectedPlanet,
                        )
                    }
                }

                // The fallback sheet is a modal surface; labels must not show through it.
                if (!destinationsOpen) {
                    val labels = computePlanetLabels(
                        planets = planets,
                        viewProj = viewProj,
                        viewport = Size(viewportW, viewportH),
                        timeSec = sceneState.elapsedTimeSec,
                        textMeasurer = textMeasurer,
                        textStyle = labelTextStyle,
                        density = this,
                        selectedTarget = selectedPlanet?.target,
                        topInsetPx = LabelTopInset.toPx(),
                        bottomInsetPx = LabelBottomInset.toPx(),
                        obstacle = centralStarObstacle(viewProj, viewportW, viewportH, centralStar),
                    )
                    labels.forEach { label ->
                        drawPlanetLabel(label, colors)
                    }
                }
            },
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = CelestialTheme.spacing.screenEdge, vertical = CelestialTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Celestial",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.starlight,
                )
                Text(
                    text = "Tap a planet or its label",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.moonlight,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(CelestialTheme.spacing.sm)) {
                GlassIconButton(
                    icon = Icons.AutoMirrored.Filled.List,
                    contentDescription = "Destinations",
                    onClick = { destinationsOpen = true },
                )
                GlassIconButton(
                    icon = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    onClick = onOpenSettings,
                )
            }
        }

        if (destinationsOpen) {
            DestinationsSheet(
                onDismiss = { destinationsOpen = false },
                onSelectDestination = { target ->
                    destinationsOpen = false
                    onSelectPlanet(target)
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DestinationsSheet(
    onDismiss: () -> Unit,
    onSelectDestination: (NavTarget) -> Unit,
) {
    val colors = CelestialTheme.colors
    val spacing = CelestialTheme.spacing
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        contentColor = colors.starlight,
        scrimColor = Color.Black.copy(alpha = 0.62f),
        dragHandle = null,
    ) {
        CelestialGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.lg, vertical = spacing.lg),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Destinations",
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.starlight,
                        )
                        Text(
                            text = "Choose where to travel",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.moonlight,
                        )
                    }
                    GlassIconButton(
                        icon = Icons.Filled.Close,
                        contentDescription = "Close destinations",
                        onClick = onDismiss,
                    )
                }

                Spacer(modifier = Modifier.height(spacing.md))

                DestinationRegistry.entries.forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .clickable(role = Role.Button) { onSelectDestination(entry.target) }
                            .padding(horizontal = spacing.md, vertical = spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(entry.primaryColor.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = entry.icon,
                                contentDescription = null,
                                tint = entry.secondaryColor,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(spacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.label,
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.starlight,
                            )
                            Text(
                                text = entry.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.moonlight,
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = colors.moonlight,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(spacing.sm))
            }
        }
    }
}

private fun centralStarObstacle(
    viewProj: Matrix4,
    viewportW: Float,
    viewportH: Float,
    centralStar: CentralStar3D,
): Rect? {
    val projected = viewProj.project(centralStar.position, viewportW, viewportH)
    if (projected.isBehind) return null
    val scale = (1000f / projected.w).coerceIn(10f, 300f)
    val radius = centralStar.radius * scale * 1.15f
    if (radius <= 0f) return null
    return Rect(
        left = projected.x - radius,
        top = projected.y - radius,
        right = projected.x + radius,
        bottom = projected.y + radius,
    )
}

private fun computePlanetLabels(
    planets: List<Planet3D>,
    viewProj: Matrix4,
    viewport: Size,
    timeSec: Float,
    textMeasurer: TextMeasurer,
    textStyle: TextStyle,
    density: Density,
    selectedTarget: NavTarget?,
    topInsetPx: Float,
    bottomInsetPx: Float,
    obstacle: Rect?,
): List<PlanetLabelLayout> {
    if (planets.isEmpty() || viewport.width < 1f || viewport.height < 1f) return emptyList()

    val horizontalPadding = with(density) { LabelHorizontalPadding.toPx() }
    val verticalPadding = with(density) { LabelVerticalPadding.toPx() }
    val gap = with(density) { LabelGapAbovePlanet.toPx() }
    val sideMargin = with(density) { LabelSideMargin.toPx() }
    val collisionGap = with(density) { LabelCollisionGap.toPx() }

    val occupied = mutableListOf<Rect>()
    obstacle?.let { occupied.add(it) }

    val candidates = planets.mapNotNull { planet ->
        val position = planet.calculatePosition(timeSec)
        val projected = viewProj.project(position, viewport.width, viewport.height)
        if (projected.isBehind) return@mapNotNull null
        // A label is only shown while its planet is on screen, so a clamped pill can never float
        // detached from an off-screen planet.
        if (projected.x !in 0f..viewport.width || projected.y !in 0f..viewport.height) {
            return@mapNotNull null
        }
        val screenRadius = (planet.radius * (1000f / projected.w)).coerceIn(4f, 220f)
        Triple(planet, projected, screenRadius)
    }.sortedBy { it.second.w }
        .take(MaxVisibleLabels)

    return candidates.mapNotNull { (planet, projected, screenRadius) ->
        val textLayout = textMeasurer.measure(planet.name, textStyle)
        val baseWidth = textLayout.size.width + horizontalPadding * 2f
        val baseHeight = textLayout.size.height + verticalPadding * 2f

        val scale = (0.86f + screenRadius / 150f).coerceIn(0.86f, 1.04f)
        val width = baseWidth * scale
        val height = baseHeight * scale

        val distanceFade = (0.5f + (screenRadius - 4f) / 44f).coerceIn(0.5f, 1f)
        val selected = planet.target == selectedTarget
        val alpha = if (selected) maxOf(distanceFade, 0.92f) else distanceFade

        val minCenterX = sideMargin + width / 2f
        val maxCenterX = viewport.width - sideMargin - width / 2f
        val centerX = projected.x.coerceIn(minCenterX, maxCenterX)

        val preferredY = projected.y - screenRadius - gap - height / 2f
        val minCenterY = topInsetPx + height / 2f
        val maxCenterY = (viewport.height - bottomInsetPx - height / 2f)
            .coerceAtLeast(minCenterY)

        val step = height + collisionGap
        val offsets = floatArrayOf(0f, -step, step, -2f * step, 2f * step)
        var placed: Rect? = null
        for (offset in offsets) {
            val centerY = (preferredY + offset).coerceIn(minCenterY, maxCenterY)
            val rect = Rect(
                left = centerX - width / 2f,
                top = centerY - height / 2f,
                right = centerX + width / 2f,
                bottom = centerY + height / 2f,
            )
            if (occupied.none { it.overlaps(rect) }) {
                placed = rect
                break
            }
        }
        val rect = placed ?: run {
            val centerY = preferredY.coerceIn(minCenterY, maxCenterY)
            Rect(
                left = centerX - width / 2f,
                top = centerY - height / 2f,
                right = centerX + width / 2f,
                bottom = centerY + height / 2f,
            )
        }
        occupied.add(rect.inflate(collisionGap))

        PlanetLabelLayout(
            planet = planet,
            rect = rect,
            textLayout = textLayout,
            baseWidth = baseWidth,
            baseHeight = baseHeight,
            alpha = alpha,
            scale = scale,
            selected = selected,
        )
    }
}

private fun expandToMinimumTouchTarget(rect: Rect, minSize: Float): Rect {
    val dx = ((minSize - rect.width) / 2f).coerceAtLeast(0f)
    val dy = ((minSize - rect.height) / 2f).coerceAtLeast(0f)
    return Rect(rect.left - dx, rect.top - dy, rect.right + dx, rect.bottom + dy)
}

private fun DrawScope.drawPlanetLabel(
    label: PlanetLabelLayout,
    colors: CelestialColors,
) {
    val center = label.rect.center
    val baseTopLeft = Offset(
        x = center.x - label.baseWidth / 2f,
        y = center.y - label.baseHeight / 2f,
    )
    val cornerRadius = CornerRadius(label.baseHeight / 2f)
    val fillAlpha = if (label.selected) 0.90f else 0.80f
    val borderColor = if (label.selected) colors.cosmicViolet else colors.glassBorder
    val borderAlpha = (if (label.selected) 0.85f else 0.35f) * label.alpha
    val glowExtension = 3.dp.toPx()
    val textLeft = baseTopLeft.x + LabelHorizontalPadding.toPx()
    val textTop = baseTopLeft.y + LabelVerticalPadding.toPx()

    withTransform({
        scale(label.scale, label.scale, center)
    }) {
        if (label.selected) {
            drawRoundRect(
                color = colors.cosmicViolet,
                alpha = 0.22f * label.alpha,
                topLeft = Offset(baseTopLeft.x - glowExtension, baseTopLeft.y - glowExtension),
                size = Size(
                    width = label.baseWidth + glowExtension * 2f,
                    height = label.baseHeight + glowExtension * 2f,
                ),
                cornerRadius = CornerRadius((label.baseHeight + glowExtension * 2f) / 2f),
            )
        }
        drawRoundRect(
            color = colors.deepSurface,
            alpha = fillAlpha * label.alpha,
            topLeft = baseTopLeft,
            size = Size(label.baseWidth, label.baseHeight),
            cornerRadius = cornerRadius,
        )
        drawRoundRect(
            color = borderColor,
            alpha = borderAlpha,
            topLeft = baseTopLeft,
            size = Size(label.baseWidth, label.baseHeight),
            cornerRadius = cornerRadius,
            style = Stroke(width = 1.dp.toPx()),
        )
        drawText(
            textLayoutResult = label.textLayout,
            topLeft = Offset(textLeft, textTop),
            color = colors.starlight,
            alpha = label.alpha,
        )
    }
}

private fun DrawScope.drawStarfield(
    starfield: Starfield3D,
    viewProj: Matrix4,
    viewportW: Float,
    viewportH: Float,
    timeSec: Float,
    reducedMotion: Boolean,
) {
    starfield.stars.forEach { star ->
        val proj = viewProj.project(star.position, viewportW, viewportH)
        if (!proj.isBehind && proj.x in 0f..viewportW && proj.y in 0f..viewportH) {
            val twinkle = if (reducedMotion) 1f else
                0.7f + 0.3f * sin(timeSec * star.twinkleSpeed + star.twinklePhase)
            val alpha = (star.brightness * twinkle).coerceIn(0.1f, 1f)
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = star.size,
                center = Offset(proj.x, proj.y),
            )
        }
    }
}

private fun DrawScope.drawCentralStar(
    star: CentralStar3D,
    viewProj: Matrix4,
    viewportW: Float,
    viewportH: Float,
    timeSec: Float,
) {
    val proj = viewProj.project(star.position, viewportW, viewportH)
    if (proj.isBehind) return

    val scale = (1000f / proj.w).coerceIn(10f, 300f)
    val starScreenRadius = star.radius * scale

    val pulse = 1f + 0.04f * sin(timeSec * 2f)

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                star.outerCoronaColor.copy(alpha = 0.4f),
                star.innerCoronaColor.copy(alpha = 0.2f),
                Color.Transparent,
            ),
            center = Offset(proj.x, proj.y),
            radius = starScreenRadius * 2.8f * pulse,
        ),
        radius = starScreenRadius * 2.8f * pulse,
        center = Offset(proj.x, proj.y),
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                star.coreColor,
                star.innerCoronaColor,
                star.outerCoronaColor.copy(alpha = 0.6f),
            ),
            center = Offset(proj.x, proj.y),
            radius = starScreenRadius * pulse,
        ),
        radius = starScreenRadius * pulse,
        center = Offset(proj.x, proj.y),
    )
}

private fun DrawScope.drawOrbitalTracks(
    planets: List<Planet3D>,
    viewProj: Matrix4,
    viewportW: Float,
    viewportH: Float,
) {
    planets.forEach { planet ->
        val path = Path()
        var first = true
        val steps = 64
        val incRad = Math.toRadians(planet.inclinationDeg.toDouble()).toFloat()

        for (i in 0..steps) {
            val angle = i * 2f * Math.PI.toFloat() / steps
            val rawX = planet.orbitRadius * cos(angle)
            val rawZ = planet.orbitRadius * sin(angle)
            val x = rawX
            val y = rawZ * sin(incRad)
            val z = rawZ * cos(incRad)

            val proj = viewProj.project(Vector3(x, y, z), viewportW, viewportH)
            if (!proj.isBehind) {
                if (first) {
                    path.moveTo(proj.x, proj.y)
                    first = false
                } else {
                    path.lineTo(proj.x, proj.y)
                }
            }
        }
        drawPath(
            path = path,
            color = planet.primaryColor.copy(alpha = 0.18f),
            style = Stroke(width = 1.2.dp.toPx()),
        )
    }
}

private fun DrawScope.drawPlanet3D(
    planet: Planet3D,
    position: Vector3,
    projected: ProjectedPoint,
    centralStarPos: Vector3,
    isSelected: Boolean,
) {
    val scale = (1000f / projected.w).coerceIn(6f, 220f)
    val planetRadiusPx = planet.radius * scale
    val center = Offset(projected.x, projected.y)

    if (planet.hasRings) {
        val ringInnerPx = planet.ringInnerRadius * scale
        val ringOuterPx = planet.ringOuterRadius * scale
        drawCircle(
            color = planet.ringColor,
            radius = ringOuterPx,
            center = center,
            style = Stroke(width = ringOuterPx - ringInnerPx),
        )
    }

    val starDir = (centralStarPos - position).normalized()
    val lightOffset = Offset(starDir.x * planetRadiusPx * 0.4f, -starDir.y * planetRadiusPx * 0.4f)

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                planet.secondaryColor,
                planet.primaryColor,
                Color(0xFF030308),
            ),
            center = center + lightOffset,
            radius = planetRadiusPx * 1.3f,
        ),
        radius = planetRadiusPx,
        center = center,
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                planet.atmosphereColor.copy(alpha = 0.5f),
                planet.atmosphereColor.copy(alpha = 0.15f),
                Color.Transparent,
            ),
            center = center,
            radius = planetRadiusPx * 1.35f,
        ),
        radius = planetRadiusPx * 1.35f,
        center = center,
    )

    if (isSelected) {
        drawCircle(
            color = planet.atmosphereColor,
            radius = planetRadiusPx * 1.5f,
            center = center,
            style = Stroke(width = 2.dp.toPx()),
        )
    }
}
