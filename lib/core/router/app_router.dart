import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../features/auth/presentation/pages/auth_page.dart';
import '../../features/blueprint/presentation/pages/blueprint_page.dart';
import '../../features/decoder/presentation/pages/decoder_page.dart';
import '../../features/appointments/presentation/pages/appointment_navigator_page.dart';
import '../../services/debug_logger.dart';
import '../../shared/widgets/app_shell.dart';

// ── Route names ──────────────────────────────────────────────────────────────

abstract final class AppRoute {
  static const String auth = '/auth';
  static const String home = '/';
  static const String blueprint = '/blueprint';
  static const String decoder = '/decoder';
  static const String appointments = '/appointments';
}

// ── Provider ─────────────────────────────────────────────────────────────────

final appRouterProvider = Provider<GoRouter>((ref) {
  DebugLogger().log('🔄 Initializing GoRouter');

  try {
    const initialLocation = AppRoute.auth;
    DebugLogger().log('✓ Initial location: $initialLocation');

    return GoRouter(
      initialLocation: initialLocation,
      debugLogDiagnostics: false,
      routes: [
        // ── Auth (Mobile) ────────────────────────────────────────────────────
        GoRoute(
          path: AppRoute.auth,
          builder: (context, state) {
            DebugLogger().log('→ Navigating to: ${AppRoute.auth}');
            return const AuthPage();
          },
        ),

        // ── Main shell with bottom navigation (Mobile) ───────────────────────
        ShellRoute(
          builder: (context, state, child) => AppShell(child: child),
          routes: [
            GoRoute(
              path: AppRoute.home,
              redirect: (_, __) => AppRoute.blueprint,
            ),
            GoRoute(
              path: AppRoute.blueprint,
              builder: (context, state) {
                DebugLogger().log('→ Navigating to: ${AppRoute.blueprint}');
                return const BlueprintPage();
              },
            ),
            GoRoute(
              path: AppRoute.decoder,
              builder: (context, state) {
                DebugLogger().log('→ Navigating to: ${AppRoute.decoder}');
                return const DecoderPage();
              },
            ),
            GoRoute(
              path: AppRoute.appointments,
              builder: (context, state) {
                DebugLogger().log('→ Navigating to: ${AppRoute.appointments}');
                return const AppointmentNavigatorPage();
              },
            ),
          ],
        ),
      ],

      // ── Global error page ────────────────────────────────────────────────────
      errorBuilder: (context, state) {
        DebugLogger().error('Router error: ${state.error}');
        return Scaffold(
          body: Center(
            child: Padding(
              padding: const EdgeInsets.all(24),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  const Icon(Icons.error_outline, size: 64, color: Colors.red),
                  const SizedBox(height: 16),
                  Text(
                    'Pagina niet gevonden',
                    style: Theme.of(context).textTheme.headlineSmall,
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 8),
                  Text(
                    state.error?.message ?? 'Onbekende fout',
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 24),
                  ElevatedButton(
                    onPressed: () => context.go(AppRoute.blueprint),
                    child: const Text('Terug naar home'),
                  ),
                ],
              ),
            ),
          ),
        );
      },
    );
  } catch (e, st) {
    DebugLogger().error('Failed to initialize GoRouter', st);
    rethrow;
  }
});
