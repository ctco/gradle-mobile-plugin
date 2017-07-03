/*
 * @(#)ExecInputStream.java
 *
 * Copyright C.T.Co Ltd, 33 Meistaru Street, Valdlauci, Kekava district, LV-1076, Latvia. All rights reserved.
 */

package lv.ctco.scm.utils.exec;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

final class ExecInputStream extends ByteArrayInputStream {

    ExecInputStream(String input) {
        super((input + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
    }

}
