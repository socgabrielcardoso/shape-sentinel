# Security portfolio evidence — shape-sentinel

## Focus
Java 17 + OpenCV computer vision as a case study in local-device permissions, dependency integrity, and resilient resource cleanup. This repository is not a production SOC product.

## Reproducible demonstration
1. Run `mvn test` on the repository commit recorded with the results.
2. Exercise the webcam prompt and stop action in a controlled environment.
3. Inspect exception handling, camera stream cleanup and external library downloads.
4. Record operating system, Java/OpenCV version, commit SHA, pass/fail count and sanitized logs.

## Security rubric
- Permission denied must not trigger repeated webcam access.
- Exception paths must release camera resources.
- Third-party code must be version-reviewed.
- No raw frames should be uploaded or logged unintentionally.

## Evidence boundaries
Use synthetic input and personal hardware only. Do not claim zero vulnerabilities without external review. Document unresolved threats in `docs/SECURITY_RISK_REGISTER.md`.
