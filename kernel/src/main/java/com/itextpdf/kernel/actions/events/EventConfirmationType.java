/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2021 iText Group NV
    Authors: iText Software.

    This program is offered under a commercial and under the AGPL license.
    For commercial licensing, contact us at https://itextpdf.com/sales.  For AGPL licensing, see below.

    AGPL licensing:
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.itextpdf.kernel.actions.events;

import com.itextpdf.kernel.actions.EventManager;
import com.itextpdf.kernel.actions.IBaseEvent;

/**
 * Defines the strategy of {@link AbstractProductProcessITextEvent} confirming.
 */
public enum EventConfirmationType {
    /**
     * The successful execution of the process associated with the event should be confirmed by the
     * second invocation of the {@link EventManager#onEvent(IBaseEvent)} method.
     */
    ON_DEMAND,
    /**
     * The successful execution of the process associated with the event will be confirmed during
     * the invocation of the {@link FlushPdfDocumentEvent#doAction()} method.
     */
    ON_CLOSE,
    /**
     * The process associated with the event shouldn't be confirmed.
     */
    UNCONFIRMABLE
}
