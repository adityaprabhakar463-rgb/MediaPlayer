package com.celestial.player.scene.host

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import com.celestial.player.ui.navigation.NavTarget
import com.celestial.player.ui.theme.CelestialTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CelestialUniverseScene(
    onSelectPlanet: (NavTarget) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    returningFromTarget: NavTarget? = null,
) {
    val camera = remember { OrbitCamera() }
    val starfield = remember { Starfield3D() }
    val centralStar = remember { CentralStar3D() }
    val planets = remember { Planet3D.defaultPlanets() }
    val reducedMotion = CelestialTheme.effects.reducedMotion

    val returnPlanet = remember(returningFromTarget) {
        returningFromTarget?.let { Planet3D.findPlanetForTarget(it) }
    }

    var selectedPlanet by remember { mutableStateOf<Planet3D?>(null) }
    var hoveredPlanet by remember { mutableStateOf<Planet3D?>(null) }
    var elapsedTimeSec by remember { mutableFloatStateOf(0f) }
    var isApproaching by remember { mutableStateOf(false) }
    var approachProgress by remember { mutableFloatStateOf(0f) }
    var approachStartEye by remember { mutableStateOf(Vector3.ZERO) }
    var approachTargetEye by remember { mutableStateOf(Vector3.ZERO) }

    var isPullingBack by remember { mutableStateOf(returnPlanet != null) }
    var pullbackProgress by remember { mutableFloatStateOf(0f) }

    var totalDragDistance by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(reducedMotion) {
        var lastTime = 0L
        while (true) {
            withFrameNanos { nanos ->
                if (lastTime != 0L) {
                    val deltaSec = (nanos - lastTime) / 1_000_000_000f
                    if (!reducedMotion) {
                        elapsedTimeSec += deltaSec
                    }
                    camera.updateInertia()
                    if (isApproaching) {
                        approachProgress = (approachProgress + deltaSec * 1.8f).coerceAtMost(1f)
                        val eye = Vector3.lerp(approachStartEye, approachTargetEye, approachProgress)
                        val targetPlanet = selectedPlanet
                        if (targetPlanet != null) {
                            camera.target = Vector3.lerp(
                                Vector3.ZERO,
                                targetPlanet.calculatePosition(elapsedTimeSec),
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
                        val planetPos = returnPlanet.calculatePosition(elapsedTimeSec)
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
            .semantics { contentDescription = "3D Celestial Solar System Universe" },
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
                                            val planetPos = planet.calculatePosition(elapsedTimeSec)
                                            val t = ray.intersectsSphere(planetPos, planet.radius * 1.5f)
                                            if (t != null && t < closestDist) {
                                                closestDist = t
                                                closestPlanet = planet
                                            }
                                        }

                                        val targetPlanet = closestPlanet
                                        if (targetPlanet != null) {
                                            selectedPlanet = targetPlanet
                                            hoveredPlanet = targetPlanet
                                            isApproaching = true
                                            approachProgress = 0f
                                            approachStartEye = camera.calculateEye()
                                            val planetPos = targetPlanet.calculatePosition(elapsedTimeSec)
                                            val viewDir = (camera.calculateEye() - planetPos).normalized()
                                            approachTargetEye = planetPos + viewDir * (targetPlanet.radius * 3.2f)
                                        } else {
                                            hoveredPlanet = null
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

                drawStarfield(starfield, viewProj, viewportW, viewportH, elapsedTimeSec, reducedMotion)

                drawCentralStar(centralStar, viewProj, viewportW, viewportH, elapsedTimeSec)

                drawOrbitalTracks(planets, viewProj, viewportW, viewportH)

                val sortedPlanets = planets.map { planet ->
                    val pos = planet.calculatePosition(elapsedTimeSec)
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
                    color = CelestialTheme.colors.starlight,
                )
                Text(
                    text = "Tap a planet to enter destination",
                    style = MaterialTheme.typography.bodySmall,
                    color = CelestialTheme.colors.moonlight,
                )
            }
            GlassIconButton(
                icon = Icons.Filled.Settings,
                contentDescription = "Settings",
                onClick = onOpenSettings,
            )
        }

        val activePlanet = hoveredPlanet ?: selectedPlanet
        AnimatedVisibility(
            visible = activePlanet != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp),
        ) {
            val planet = activePlanet ?: return@AnimatedVisibility
            CelestialGlassSurface(
                modifier = Modifier.padding(horizontal = 24.dp),
                selected = true,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = planet.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = CelestialTheme.colors.starlight,
                    )
                    Text(
                        text = planet.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = CelestialTheme.colors.moonlight,
                    )
                }
            }
        }
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
