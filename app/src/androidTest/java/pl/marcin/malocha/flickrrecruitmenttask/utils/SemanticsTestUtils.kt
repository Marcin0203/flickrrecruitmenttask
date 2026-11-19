package pl.marcin.malocha.flickrrecruitmenttask.utils

import androidx.compose.ui.test.SemanticsNodeInteraction

/** True if the node exists in the semantics tree. Useful in `waitUntil`, which needs a condition and not an assertion. */
fun SemanticsNodeInteraction.isPresent(): Boolean = runCatching { fetchSemanticsNode() }.isSuccess
