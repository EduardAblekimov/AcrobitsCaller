package net.acrobits.interview.test.core.utils

object Constants {
    // best practice: move to CI secrets
    const val LICENSE_KEY = "r1sl6vl1gms0v014t922vuc0r2"

    val PROVISIONING_XML = """
        <?xml version="1.1"?>
        <provisioning>
            <saas>
                <identifier>$LICENSE_KEY</identifier>
            </saas>
        </provisioning>
    """.trimIndent()
}