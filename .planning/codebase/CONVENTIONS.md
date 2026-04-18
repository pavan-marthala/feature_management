# Conventions
Date: 2026-04-18

## Backend Conventions
- **Reactive Types**: Services and controllers consistently return `Mono<T>` or `Flux<T>`.
- **Validation**: JSR-303 annotations (e.g., `@Valid`) are used on REST controller inputs.
- **DTOs**: OpenAPI generator is used to build standardized request/response models (`org.feature.management.models`).
- **Lombok**: Heavily used to reduce boilerplate (`@Slf4j`, `@RequiredArgsConstructor`, `@Builder`).
- **Code Style**: Checkstyle validation is integrated into the Maven build phase via `checkstyle.xml`.

## Frontend Conventions
- **Composition API**: Relies on modern Vue 3 composition functions and setup script blocks.
- **State Management**: Uses Pinia for structured, reactive state.
- **Tooling**: Uses `oxlint` and `eslint` for fast, strict linting, and `oxfmt` for formatting.
- **Styling**: Tailwind CSS classes are applied directly within templates.
