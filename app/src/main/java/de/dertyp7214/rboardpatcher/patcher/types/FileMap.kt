package de.dertyp7214.rboardpatcher.patcher.types

data class FileMap(
    val patches: ArrayList<String> = arrayListOf(),
    val patchFiles: HashMap<String, List<String>> = hashMapOf()
)
