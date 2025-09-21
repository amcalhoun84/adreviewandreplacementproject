# Ad Review, Brand Detection and Replacement ProjectABD
Advertisement/Brand Detector | CSCI 576 Multimedia Project | Instructor: Parag Havaldar | Demo date: May 2022

This project was built as part of a graduate capstone under extreme constraints — roughly two weeks of development, while I was also working, and collaborating with teammates who were not professional engineers. On top of that, I was not very familiar with Java at the time, and the class material itself leaned heavily on abstract computer vision concepts and linear algebra, which can be challenging to re-engage with if it’s been a while. The result isn’t production-ready, but it does demonstrate how I can deliver results under pressure, ramp quickly on unfamiliar tools, and make pragmatic tradeoffs to ship something functional.

## What It Does:
The system performs audiovisual analysis to detect advertising segments within video streams and replace them.

Extracts visual and audio features (e.g., histograms, SIFT/FLANN matching).

Identifies transitions using threshold-based heuristics.

Applies consensus logic across frames to minimize false positives.

Supports basic replacement of detected segments with ads matching the detected companies.

## Constraints & Tradeoffs:
Timeframe: Built in ~2-3 weeks.

Team composition: Worked with non-professional collaborators.

Language: Java, which I was not deeply familiar with at the time.

Conceptual overhead: Heavy reliance on abstract CV/linear algebra concepts, which made implementation extra challenging.

Focus: Optimized for delivering a working pipeline rather than production-grade polish. As a result, the code has rough edges: long stateful methods, scattered magic numbers, minimal error handling, no tests, and tight coupling between components.

## What I’d Do Differently Now:

With industry experience, I can look back and see how to make this system far more maintainable and scalable:

Abstraction & modularity: Introduce clear interfaces (e.g., a TransitionScorer strategy class) instead of piling logic into single methods.

Configuration: Replace hardcoded thresholds with centralized configs or constants.

Reliability: Add structured logging, proper error handling, and use try-with-resources for I/O.

Testability: Write unit and snapshot tests for frame extraction and transition detection.

Collaboration scaffolding: Add linting, style guides, and lightweight CI to make teamwork smoother.

## Why Keep This Here:

This project captures an important point in my growth as an engineer:

The ability to deliver under constraints. The ability to adapt to an unfamiliar language and steep theoretical material quickly. And most importantly, the ability to reflect on past work and explain how I’d improve it today. I also wrote a lot of the infrastructure, entropy heuristics to detect shifts from raw footage to advertisements, calculation formulas, shot logic, and the players.

## Group members:
Danny Diaz Ayon

Andrew Calhoun

Xiaoyu Zhang (Derek)
