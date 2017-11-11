/*
 * HeadsUp Agile
 * Copyright 2015 Heads Up Development.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.headsupdev.agile.web.components.issues;

import org.hibernate.criterion.Criterion;

/**
 * TODO: Document Me
 * <p/>
 * Created: 04/09/2015
 *
 * @author Andrew Williams
 * @since 2.1
 */
public interface IssueFilter {
    Criterion getStatusCriterion();

    Criterion getAssignmentCriterion();

    Criterion getDateCriterionUpdated();

    Criterion getDateCriterionCreated();
}
