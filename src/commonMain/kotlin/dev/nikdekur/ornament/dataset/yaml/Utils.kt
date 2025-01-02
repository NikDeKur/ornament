package dev.nikdekur.ornament.dataset.yaml

public typealias YamlString = String

public fun YamlString.addRootSection(name: String): YamlString = "$name:\n" + this.prependIndent("  ")