/*
 * Dataverse Network - A web application to distribute, share and analyze quantitative data.
 * Copyright (C) 2007
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 *  along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation,Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package edu.harvard.iq.dvn.core.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *
 * Compares String objects alphabetically, except if both Strings begin with a number, 
 * then compares numerically.  If one String begins with a number and the other doesn't, then 
 * the number precedes the non-number.
 *
 *  @throws ClassCastException if either parameter is not a String
 * @author Ellen Kraffmiller
 */
public class AlphaNumericComparator implements Comparator, java.io.Serializable {

    public AlphaNumericComparator() {
    }

    public int compare(Object o1, Object o2) {
        List tokenizedList1 = getTokenizedList((String) o1);
        List tokenizedList2 = getTokenizedList((String) o2);

        for (int i = 0; i < Math.min(tokenizedList1.size(), tokenizedList2.size()); i++) {
            Object token1 = tokenizedList1.get(i);
            Object token2 = tokenizedList2.get(i);

            if (token1 instanceof BigDecimal) {
                if (token2 instanceof BigDecimal) {
                    int compareVal = ((BigDecimal) token1).compareTo((BigDecimal) token2);
                    if (compareVal != 0) {
                        return compareVal;
                    }

                } else {
                    return -1; // token1 is a number, token2 is not
                }

            } else if (token2 instanceof BigDecimal) {
                return 1; // token2 is a number, token1 is  not

            } else {
                int compareVal = ((String) token1).compareTo((String) token2);
                if (compareVal != 0) {
                    return compareVal;
                }
            }
        }

        // they match up, so compare based on who stll has tokens
        return new Integer(tokenizedList1.size()).compareTo(new Integer(tokenizedList2.size()));
    }

    /* this method returns a list of the String as tokens of BigDecimals and Strings
    // a slight challenge is to determine the intent of the user, e.g. a '-' could be for a negative number
    // or just be for demarcation (e.g A-1, A-2)
    //
    // some of the logic incorporated below:
    // a '.' is part of a number if if it is followed by a digit and there is only one instance
    // if there are multiple instances in a number (e.g. 1.5.2 the whole thing is treated as a String)
    // a ',' is part of a number if it is both preceded and followed by a digit (we do not validate that
    // it is in a proper grouping (i.e. 1,00 is treated as 100, even if that is not a standard way of writing it)
    // a '-' is only used to denote negative if it is the first character of the String
    */
     private List getTokenizedList(String value) {
        List tokenizedList = new ArrayList();
        char[] charArray = value.trim().toCharArray();

        StringBuffer currentToken = new StringBuffer("");
        boolean isCurrentTokenNumeric = false;

        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            boolean hasPrevChar = i > 0;
            boolean hasNextChar = i < charArray.length - 1;

            if (Character.isDigit(c) ||
                    (c == '.' && hasNextChar && Character.isDigit(charArray[i + 1])) ||
                    (c == ',' && hasNextChar && hasPrevChar && Character.isDigit(charArray[i - 1]) && Character.isDigit(charArray[i + 1])) ||
                    (c == '-' && !hasPrevChar && hasNextChar && Character.isDigit(charArray[i + 1]))) {

                if (!isCurrentTokenNumeric) { // reset
                    tokenizedList.add(currentToken.toString());
                    currentToken = new StringBuffer("");
                }

                if (c != ',') { // if comma, don't append as it's just a visual separator
                    currentToken.append(c);
                }
                isCurrentTokenNumeric = true;
                
            } else {

                if (isCurrentTokenNumeric) { // reset
                    try {
                        tokenizedList.add(new BigDecimal(currentToken.toString()));
                    } catch (NumberFormatException nfe) {
                        tokenizedList.add(currentToken.toString()); // something went wrong, but go ahead and add a a String
                    }
                    currentToken = new StringBuffer("");
                }

                currentToken.append(c);
                isCurrentTokenNumeric = false;
            }
        }
        
        // add the last token to the list
        if (isCurrentTokenNumeric) {
            try {
                tokenizedList.add(new BigDecimal(currentToken.toString()));
            } catch (NumberFormatException nfe) {
                tokenizedList.add(currentToken.toString()); // something went wrong, but go ahead and add a a String
            }
        } else {
            tokenizedList.add(currentToken.toString());   
        }

        return tokenizedList;

    }


    public boolean equals(Object obj) {
        return (obj instanceof AlphaNumericComparator);
    }

}
