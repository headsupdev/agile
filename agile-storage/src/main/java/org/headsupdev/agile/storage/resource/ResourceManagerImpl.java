/*
 * HeadsUp Agile
 * Copyright 2009-2012 Heads Up Development Ltd.
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

package org.headsupdev.agile.storage.resource;

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.storage.issues.*;
import org.headsupdev.support.java.DateUtil;
import org.hibernate.Criteria;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility methods for working with DurationWorked calculations.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class ResourceManagerImpl
{
    private static Logger log = Manager.getLogger( ResourceManagerImpl.class.getName() );

    /**
     * this will always return a duration object. it will be the most appropriate 'estimated time' remaining
     * for the issue in question on the dayInQuestion.
     *
     * @param issue
     * @param dayInQuestion
     * @return
     */
    public Duration lastEstimateForDay( Issue issue, Date dayInQuestion )
    {

        Date endOfDayInQuestion = DateUtil.getEndOfDate(Calendar.getInstance(), dayInQuestion);
        Date lastEstimateDate = null; // used to know where our current estimate was based on
        Duration estimate = null;

        for ( DurationWorked worked : issue.getTimeWorked() )
        {
            Date workedDay = worked.getDay();
            if ( workedDay == null || worked.getUpdatedRequired() == null )
            {
                continue;
            }

            // for this Worked object to be considered valid it must be worked before the day in question
            if ( workedDay.before( endOfDayInQuestion ) )
            {
                if ( lastEstimateDate == null || !workedDay.before( lastEstimateDate ) )
                {
                    estimate = worked.getUpdatedRequired();
                    lastEstimateDate = workedDay;
                }
            }
        }

        if ( estimate == null )
        {
            // if the issue was created before the dayInQuestion then we report the original issue time estimate
            // or if we want to backtrack the originalTimeEstimate
            if ( issue.getIncludeInInitialEstimates() || issue.getCreated().before( endOfDayInQuestion ) )
            {
                if ( issue.getTimeRequired() != null && ( issue.getTimeWorked() == null || issue.getTimeWorked().size() == 0 ) )
                {
                    estimate = issue.getTimeRequired();
                }
                else if ( issue.getTimeEstimate() != null )
                {
                    estimate = issue.getTimeEstimate();
                }
            }
            else
            {
                // otherwise we report 0 hours for this issue vs dayInQuestion
                estimate = new Duration( 0 );
            }
        }

        return estimate;
    }

    public Duration lastEstimateForIssue( Issue issue )
    {
        Duration estimate = issue.getTimeEstimate();

        Date lastEstimateDate = null;
        for ( DurationWorked worked : issue.getTimeWorked() )
        {
            if ( worked.getDay() == null || worked.getUpdatedRequired() == null )
            {
                continue;
            }

            if ( lastEstimateDate == null || ( worked.getDay() != null && !worked.getDay().before( lastEstimateDate ) ) )
            {
                estimate = worked.getUpdatedRequired();
                lastEstimateDate = worked.getDay();
            }
        }

        return estimate;
    }

    public Duration totalWorkedForDay( Issue issue, Date date )
    {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime( date );

        double total = 0d;
        Calendar cal2 = GregorianCalendar.getInstance();

        for ( DurationWorked worked : issue.getTimeWorked() )
        {
            if ( worked.getDay() == null || worked.getUpdatedRequired() == null )
            {
                continue;
            }

            cal2.setTime( worked.getDay() );
            if ( cal.get( Calendar.DATE ) == cal2.get( Calendar.DATE ) &&
                    cal.get( Calendar.MONTH ) == cal2.get( Calendar.MONTH ) &&
                    cal.get( Calendar.YEAR ) == cal2.get( Calendar.YEAR ) )
            {
                if ( worked.getWorked() != null )
                {
                    total += worked.getWorked().getHours();
                }
            }
        }

        return new Duration( total );
    }

    public Date getMilestoneStartDate( Milestone milestone )
    {
        return getIssueSetStartDate( milestone.getIssues(), milestone.getStartDate(), milestone.getDueDate() );
    }

    public Date getMilestoneGroupStartDate( MilestoneGroup group )
    {
        return getIssueSetStartDate( group.getIssues(), group.getStartDate(), group.getDueDate() );
    }

    private Date getIssueSetStartDate( Set<Issue> issues, Date start, Date due )
    {
        if ( start != null )
        {
            return start;
        }

        if ( due == null )
        {
            due = new Date();
        }

        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime( due );

        cal.add( Calendar.DATE, -14 );
        Date first = cal.getTime();

        for ( Issue issue : issues )
        {
            for ( DurationWorked worked : issue.getTimeWorked() )
            {
                if ( worked.getDay() == null )
                {
                    continue;
                }

                if ( !first.before( worked.getDay() ) )
                {
                    first = worked.getDay();
                }
            }
        }

        return first;
    }

    public List<Date> getMilestoneDates( Milestone milestone, boolean includeDayBefore )
    {
        return getIssueSetDates( getMilestoneStartDate(milestone), milestone.getDueDate(),
                milestone.getProject(), includeDayBefore );
    }

    public List<Date> getMilestoneGroupDates( MilestoneGroup group, boolean includeDayBefore )
    {
        return getIssueSetDates( getMilestoneGroupStartDate(group), group.getDueDate(),
                group.getProject(), includeDayBefore );
    }

    private List<Date> getIssueSetDates( Date start, Date due, Project project, boolean includeDayBefore )
    {
        // some prep work to make sure we have valid dates for start and end of milestone
        List<Date> dates = new LinkedList<Date>();
        if ( due == null || start == null )
        {
            return dates;
        }

        Calendar calendar = Calendar.getInstance();
        Date confirmedEnd = DateUtil.getEndOfDate( calendar, due );
        Date confirmedStart = DateUtil.getStartOfDate( calendar, start );

        final boolean ignoreWeekend = Boolean.parseBoolean( project.getConfigurationValue(
                StoredProject.CONFIGURATION_TIMETRACKING_IGNOREWEEKEND ) );

        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime( confirmedStart );

        if ( includeDayBefore )
        {
            // return the day before as a starting point for estimates - work can be logged after this, contributing to
            // a lower value at the end of the first day
            cal.add( Calendar.DATE, -1 );
        }
        boolean estimateDay = Boolean.parseBoolean( project.getConfigurationValue(
                StoredProject.CONFIGURATION_TIMETRACKING_BURNDOWN ) );
        for ( Date date = cal.getTime(); date.before( confirmedEnd ); date = cal.getTime() )
        {
            if ( ignoreWeekend && !estimateDay && ( cal.get( Calendar.DAY_OF_WEEK ) == Calendar.SATURDAY ||
                    cal.get( Calendar.DAY_OF_WEEK ) == Calendar.SUNDAY ) )
            {
                cal.add( Calendar.DATE, 1 );
                continue;
            }

            dates.add( date );

            cal.add( Calendar.DATE, 1 );
            estimateDay = false;
        }

        return dates;
    }

    /**
     * this will return an array of durations that match to the dates on the milestone in date order.
     * index 0 will show the effort remaining at end of day 1 , etc.
     *
     * @param milestone
     * @return
     */
    public Duration[] getMilestoneEffortRequired( Milestone milestone )
    {
        if ( milestone == null )
        {
            return null;
        }

        List<Date> milestoneDates = getMilestoneDates( milestone, false );
        return getIssueSetEffortRequired( milestone.getIssues(), milestoneDates );
    }

    /**
     * this will return an array of durations that match to the dates on the milestone group in date order.
     * index 0 will show the effort remaining at end of day 1 , etc.
     *
     * @param group
     * @return
     */
    public Duration[] getMilestoneGroupEffortRequired( MilestoneGroup group )
    {
        if ( group == null )
        {
            return null;
        }

        List<Date> groupDates = getMilestoneGroupDates( group, false );
        return getIssueSetEffortRequired( group.getIssues(), groupDates );
    }

    private Duration[] getIssueSetEffortRequired( Set<Issue> issues, List<Date> milestoneDates )
    {
        if ( milestoneDates == null || milestoneDates.size() == 0 )
        {
            return null;
        }
        // TODO fix getMilestoneDates to work correctly ....
        // manually add the start date to the milestone dates list as getMilestoneDates acts weird.
        Calendar calendar = Calendar.getInstance();
        calendar.setTime( milestoneDates.get( 0 ) );
        calendar.add( Calendar.DATE, -1 );

        List<Date> dates = new ArrayList<Date>();
        dates.add( calendar.getTime() );
        for ( Date date : milestoneDates )
        {
            dates.add( date );
        }

        Duration[] effortRequired = new Duration[ dates.size() ];
        // initialise the returnArray if there are no issues on milestone.
        if ( issues == null || issues.size() == 0 )
        {
            for ( int i = 0; i < effortRequired.length; i++ )
            {
                effortRequired[ i ] = new Duration( 0 );
            }
            return effortRequired;
        }

        // iterate over each day and calculate the effort remaining.
        int dayIndex = 0;
        for ( Date date : dates )
        {
            double totalHoursForDay = 0;
            for ( Issue issue : issues )
            {
                Duration lastEstimate = lastEstimateForDay( issue, date );
                if ( lastEstimate != null )
                {
                    totalHoursForDay += lastEstimate.getHours();
                }
            }
            effortRequired[ dayIndex ] = new Duration( totalHoursForDay );

            dayIndex++;
        }

        return effortRequired;
    }

    public double getMilestoneCompleteness( Milestone milestone )
    {
        return getIssueListCompleteness( milestone.getIssues(), milestone.getProject() );
    }

    public double getMilestoneGroupCompleteness( MilestoneGroup group )
    {
        return getIssueListCompleteness( group.getIssues(), group.getProject() );
    }

    private double getIssueListCompleteness( Set<Issue> issues, Project project )
    {
        final boolean timeEnabled = Boolean.parseBoolean( project.getConfigurationValue(
                StoredProject.CONFIGURATION_TIMETRACKING_ENABLED ) );
        final boolean timeBurndown = Boolean.parseBoolean( project.getConfigurationValue(
                StoredProject.CONFIGURATION_TIMETRACKING_BURNDOWN ) );

        double done = 0;
        double total = 0;
        for ( Issue issue : issues )
        {
            double issueHours = .25;
            if ( timeEnabled && issue.getTimeEstimate() != null && issue.getTimeEstimate().getHours() > 0 )
            {
                issueHours = issue.getTimeEstimate().getHours();
            }
            total += issueHours;

            if ( issue.getStatus() >= Issue.STATUS_RESOLVED )
            {
                done += issueHours;
                continue;
            }

            if ( !timeEnabled )
            {
                // add nothing to the done count for open issues...
                continue;
            }

            if ( timeBurndown )
            {
                Duration left = lastEstimateForIssue( issue );
                if ( left != null )
                {
                    double leftHours = left.getHours();
                    if ( issue.getStatus() < Issue.STATUS_RESOLVED && leftHours < issueHours )
                    {
                        leftHours = issueHours;
                    }

                    done += Math.max( issueHours - leftHours, 0 );
                }
            }
            else
            {
                Duration worked = lastEstimateForIssue( issue );
                if ( worked != null )
                {
                    done += Math.min( worked.getHours(), issueHours );
                }
            }
        }

        return done / total;
    }

    public Velocity getAverageVelocity()
    {
        if ( !averageVelocity.equals( Double.NaN ) )
        {
            return averageVelocity;
        }

        double velocities = 0.0;
        int velocityCount = 0;
        for ( User user : Manager.getSecurityInstance().getRealUsers() )
        {
            if ( !user.canLogin() )
            {
                continue;
            }

            Velocity velocity = getUserVelocity( user );
            if ( !velocity.equals( Velocity.INVALID ) )
            {
                velocities += velocity.getVelocity();
                velocityCount++;
            }
        }

        averageVelocity = new Velocity( velocities, (double) velocityCount );
        return averageVelocity;
    }

    // TODO we need to expire this once a week (or day)...
    private static Map<User, Velocity> userVelocities = new HashMap<User, Velocity>();
    private static Velocity averageVelocity = Velocity.INVALID;

    public Velocity getUserVelocity(User user)
    {
        if ( userVelocities.get( user ) != null )
        {
            return userVelocities.get( user );
        }

        List<DurationWorked> worked = getDurationWorkedForUser( user );
        Velocity velocity = calculateVelocity( worked, user, null, null );

        userVelocities.put( user, velocity );
        return velocity;
    }

    public Velocity getCurrentUserVelocity(User user)
    {
//        if ( userVelocities.get( user ) != null )
//        {
//            return userVelocities.get( user );
//        }

        Calendar cal = Calendar.getInstance();
        cal.add( Calendar.WEEK_OF_YEAR, -1 );
        List<DurationWorked> worked = getDurationWorkedForUser(user, cal.getTime(), new Date());
        Velocity velocity = calculateVelocity( worked, user, cal.getTime(), new Date() );

//        userVelocities.put( user, velocity );
        return velocity;
    }

    public Velocity getUserVelocityInWeek( User user, Date week )
    {
// TODO cache this
//        if ( userVelocities.get( user ) != null )
//        {
//            return userVelocities.get( user );
//        }

        Calendar cal = Calendar.getInstance();
        cal.setTime( week );
        cal.add( Calendar.WEEK_OF_YEAR, 1 );
        // TODO midnight next week is not included now - right?
        cal.add( Calendar.MILLISECOND, -1 );

        List<DurationWorked> worked = getDurationWorkedForUser( user, week, cal.getTime() );
        Velocity velocity = calculateVelocity( worked, user, week, cal.getTime() );

//        userVelocities.put( user, velocity );
        return velocity;
    }

    public Velocity getVelocity( List<DurationWorked> worked, Milestone milestone )
    {
        return getVelocity( worked, milestone.getStartDate(), milestone.getDueDate() );
    }

    public Velocity getVelocity( List<DurationWorked> worked, MilestoneGroup group )
    {
        return getVelocity( worked, group.getStartDate(), group.getDueDate() );
    }

    public Velocity getVelocity( List<DurationWorked> worked, Date setStart, Date setDue )
    {
        Set<User> usersWorked = new HashSet<User>();

        // TODO fix issues around resources not working days they were allocated to...
        Date start = new Date();
        Date end = new Date( 0 );
        boolean calculateRange = true;
        if ( setStart != null )
        {
            start = setStart;
            end = setDue;
            calculateRange = false;
        }

        for ( DurationWorked duration : worked )
        {
            usersWorked.add( duration.getUser() );

            if ( calculateRange )
            {
                if ( duration.getDay().before( start ) )
                {
                    start = DateUtil.getStartOfDate( Calendar.getInstance(), duration.getDay() );
                }
                if ( duration.getDay().after( end ) )
                {
                    end = DateUtil.getEndOfDate( Calendar.getInstance(), duration.getDay() );
                }
            }
        }

        double velocities = 0.0;
        int velocityCount = 0;
        for ( User user : usersWorked )
        {
            if ( user.isHiddenInTimeTracking() )
            {
                continue;
            }

            Velocity vel = calculateVelocity( worked, user, start, end );
            if ( !vel.equals( Velocity.INVALID ) )
            {
                velocities += vel.getVelocity();
                velocityCount++;
            }
        }

        return new Velocity( velocities, (double) velocityCount );
    }

    private Velocity calculateVelocity( List<DurationWorked> workedList, User user, Date start, Date end )
    {
        if ( user.isHiddenInTimeTracking() )
        {
            return Velocity.INVALID;
        }

        double estimatedHoursWorked = 0.0;
        double daysWorked = 0.0;
        Set<Date> daysSeen = new HashSet<Date>();
        Calendar cal = Calendar.getInstance();

        Set<Issue> relevantIssues = new HashSet<Issue>();
        for ( DurationWorked duration : workedList )
        {
            try
            {
                if ( duration.getIssue() != null && !relevantIssues.contains( duration.getIssue() ) )
                {
                    relevantIssues.add( duration.getIssue() );
                }
            }
            catch ( ObjectNotFoundException e )
            {
                // ignore - TODO find a better way of handling this...
            }
        }

        for ( Issue issue : relevantIssues )
        {
            if ( issue.getTimeEstimate() == null || issue.getTimeEstimate().getHours() == 0 )
            {
                continue;
            }

            double estimate = issue.getTimeEstimate().getHours();
            double hoursWorked = 0;
            double totalEstimated = 0;

            List<DurationWorked> listWorked = new ArrayList<DurationWorked>( issue.getTimeWorked() );
            Collections.sort( listWorked, new Comparator<DurationWorked>()
            {
                public int compare( DurationWorked d1, DurationWorked d2 )
                {
                    Date date1 = d1.getDay();
                    Date date2 = d2.getDay();

                    if ( date1 == null || date2 == null )
                    {
                        if ( date1 == null )
                        {
                            if ( date2 == null )
                            {
                                return 0;
                            }
                            else
                            {
                                return 1;
                            }
                        }
                        else
                        {
                            return -1;
                        }
                    }

                    if ( date1.equals( date2 ) )
                    {
                        double d1hours = 0;
                        if ( d1.getUpdatedRequired() != null )
                        {
                            d1hours = d1.getUpdatedRequired().getHours();
                        }
                        double d2hours = 0;
                        if ( d2.getUpdatedRequired() != null )
                        {
                            d2hours = d2.getUpdatedRequired().getHours();
                        }
                        return Double.compare( d1hours, d2hours );
                    }
                    return date1.compareTo( date2 );
                }
            } );

            for ( DurationWorked worked : listWorked )
            {
                if ( ( start != null && start.after( worked.getDay() ) ) ||
                        ( end != null && end.before( worked.getDay() ) ) )
                {
                    continue;
                }
                // ignore empty work but respect it's estimate
                if ( worked.getWorked() == null || worked.getWorked().getHours() == 0 )
                {
                    if ( worked.getUpdatedRequired() != null )
                    {
                        estimate = worked.getUpdatedRequired().getHours();
                    }
                    continue;
                }

                // don't count work not in the list but respect new estimates
                if ( !workedList.contains( worked ) )
                {
                    if ( worked.getUpdatedRequired() != null )
                    {
                        estimate = worked.getUpdatedRequired().getHours();
                    }
                    continue;
                }

                if ( worked.getUser().equals( user ) && worked.getDay() != null )
                {
                    hoursWorked += worked.getWorked().getHours();
                    if ( worked.getUpdatedRequired() != null )
                    {
                        totalEstimated += estimate - worked.getUpdatedRequired().getHours();
                    }

                    // check if this is a new day
                    cal.setTime( DateUtil.getStartOfDate( cal, worked.getDay() ) );
                    if ( !daysSeen.contains( cal.getTime() ) )
                    {
                        daysWorked += 1.0;
                        daysSeen.add( cal.getTime() );
                    }
                }

                if ( worked.getUpdatedRequired() != null )
                {
                    estimate = Math.min( estimate, worked.getUpdatedRequired().getHours() );
                }
            }

            if ( hoursWorked == 0 )
            {
                continue;
            }

            estimatedHoursWorked += totalEstimated;
        }

        Velocity velocity = Velocity.INVALID;
        if ( daysWorked > 0 )
        {
            velocity = new Velocity( estimatedHoursWorked, daysWorked );
        }
        log.debug( velocity.toString() );
        return velocity;
    }

    public Double getUserHoursLogged( User user )
    {
// TODO cache
//        if ( userVelocities.get( user ) != null )
//        {
//            return userVelocities.get( user );
//        }

        Double logged = calculateHoursLogged( getDurationWorkedForUser( user ), user );

//        userVelocities.put( user, logged );
        return logged;
    }

    public Double getUserHoursLoggedInWeek( User user, Date week )
    {
// TODO cache this
//        if ( userVelocities.get( user ) != null )
//        {
//            return userVelocities.get( user );
//        }

        Calendar cal = Calendar.getInstance();
        cal.setTime( week );
        cal.add( Calendar.WEEK_OF_YEAR, 1 );
        // TODO midnight next week is not included now - right?
        cal.add( Calendar.MILLISECOND, -1 );
        Double logged = calculateHoursLogged( getDurationWorkedForUser( user, week, cal.getTime() ), user );

//        userVelocities.put( user, velocity );
        return logged;
    }

    private Double calculateHoursLogged( List<DurationWorked> workedList, User user )
    {
        double total = 0;
        int daysWorked = 0;
        Set<Date> daysSeen = new HashSet<Date>();
        Calendar cal = Calendar.getInstance();

        for ( DurationWorked worked : workedList )
        {
            if ( worked.getUser().equals( user ) && worked.getWorked() != null )
            {
                total += worked.getWorked().getHours();
                if ( worked.getDay() == null )
                {
                    continue;
                }

                // check if this is a new day
                cal.setTime( worked.getDay() );
                cal.set( Calendar.HOUR_OF_DAY, 0 );
                cal.set( Calendar.MINUTE, 0 );
                cal.set( Calendar.SECOND, 0 );
                cal.set( Calendar.MILLISECOND, 0 );
                if ( !daysSeen.contains( cal.getTime() ) )
                {
                    daysWorked++;
                    daysSeen.add( cal.getTime() );
                }
            }
        }

        return total / daysWorked;
    }

    public List<DurationWorked> getDurationWorkedForUser( User user )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Criteria c = session.createCriteria( DurationWorked.class );
        c.add( Restrictions.eq( "user", user ) );
        c.add( Restrictions.gt( "worked.time", 0 ) );

        return c.list();
    }

    public List<DurationWorked> getDurationWorkedForUser( User user, Date start, Date end )
    {
        List<DurationWorked> workedList = new ArrayList<DurationWorked>();
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Criteria c = session.createCriteria( DurationWorked.class );
        c.add( Restrictions.eq( "user", user ) );
        c.add( Restrictions.gt( "worked.time", 0 ) );
        c.add( Restrictions.between("day", start, end) );

        for ( DurationWorked worked : (List<DurationWorked>) c.list() )
        {
            if ( worked.getDay().before( start ) || worked.getDay().after( end ) )
            {
                continue;
            }

            workedList.add( worked );
        }
        return workedList;
    }

    /**
     * This will sum together all duration logged against a user between start and end.
     * Depending on the types of Duration logged against this user, the smallest unit will be
     * represented in the return type.
     * <p/>
     * For example if there are two logged times, 1 hour and 1 day, the time unit
     * for the Duration returned will be composed of hours
     *
     * @return
     */
    public Duration getLoggedTimeForUser( User user, Date start, Date end )
    {

        List<DurationWorked> workedList = getDurationWorkedForUser( user, start, end );
        double hoursLogged = 0;
        for ( DurationWorked worked : workedList )
        {
            hoursLogged += worked.getWorked().getHours();
        }

        return new Duration( hoursLogged );
    }

    /**
     * Determine if an issue is going to miss it's target amount of work.
     * This is based on how much work has been logged and the initial estimate of the issue.
     *
     * @param issue The issue to check
     * @return True if the issue is over the estimated amount of work required to complete.
     */
    public boolean isIssueMissingEstimate( Issue issue )
    {
        final boolean burndown = Boolean.parseBoolean( issue.getProject().getConfigurationValue(
                StoredProject.CONFIGURATION_TIMETRACKING_BURNDOWN ) );

        if ( burndown )
        {
            if ( issue.getTimeEstimate() != null && issue.getTimeEstimate().getHours() > 0 &&
                    issue.getTimeWorked() != null )
            {
                double remain = getWorkRemainingForIssue( issue );

                return remain < 0;
            }
        }

        return false;
    }

    /**
     * Determine if an issue is going to miss it's target amount of work by a significant margin.
     * This is based on how much work has been logged and the initial estimate of the issue.
     * The margin is defined as 50% over the estimated amount of work.
     *
     * @param issue The issue to check
     * @return True if the issue is over the estimated amount of work required to complete.
     */
    public boolean isIssueSeriouslyMissingEstimate( Issue issue )
    {
        final double AMOUNT_OVER_TO_BE_SERIOUS = .5;
        final boolean burndown = Boolean.parseBoolean( issue.getProject().getConfigurationValue(
                StoredProject.CONFIGURATION_TIMETRACKING_BURNDOWN ) );

        if ( burndown )
        {
            if ( issue.getTimeEstimate() != null && issue.getTimeEstimate().getHours() > 0 &&
                    issue.getTimeWorked() != null )
            {
                double estimate = getEstimateMultiplier( issue ) * issue.getTimeEstimate().getHours();
                double remain = getWorkRemainingForIssue( issue );

                return remain < ( estimate * AMOUNT_OVER_TO_BE_SERIOUS ) * -1;
            }
        }

        return false;
    }

    protected double getWorkRemainingForIssue( Issue issue )
    {
        double estimate = getEstimateMultiplier( issue ) * issue.getTimeEstimate().getHours();
        double remain = estimate;
        for ( DurationWorked worked : issue.getTimeWorked() )
        {
            if ( worked.getWorked() != null )
            {
                remain -= worked.getWorked().getHours();
            }
        }

        if ( issue.getTimeRequired() == null ) {
            remain -= estimate;
        } else {
            remain -= issue.getTimeRequired().getHours();
        }

        return remain;
    }

    protected double getEstimateMultiplier( Issue issue )
    {
        return 1;
    }
}
