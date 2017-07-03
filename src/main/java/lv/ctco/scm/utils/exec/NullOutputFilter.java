/*
 * @(#)NullOutputFilter.java
 *
 * Copyright C.T.Co Ltd, 33 Meistaru Street, Valdlauci, Kekava district, LV-1076, Latvia. All rights reserved.
 */

package lv.ctco.scm.utils.exec;

public final class NullOutputFilter extends ExecOutputFilter {

    @Override
    public boolean matchesFilter(String line) {
        return false;
    }

}
