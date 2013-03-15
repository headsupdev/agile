package org.headsupdev.agile.storage.resource;

/**
 * A simple class representing a velocity of time done over a number of days
 * <p/>
 * Created: 14/03/2013
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class Velocity
{
    public static Velocity INVALID = new Velocity( 0.0, 0.0 )
    {
        @Override
        public Double getVelocity()
        {
            return Double.NaN;
        }
    };

    private Double estimatedHours;
    private Double daysWorked;
    private Double velocity;

    public Velocity( Double estimatedHours, Double daysWorked )
    {
        this.estimatedHours = estimatedHours;
        this.daysWorked = daysWorked;
        this.velocity = ( estimatedHours / daysWorked );
    }

    public Double getEstimatedHours()
    {
        return estimatedHours;
    }

    public Double getDaysWorked()
    {
        return daysWorked;
    }

    public Double getVelocity()
    {
        return velocity;
    }

    @Override
    public String toString()
    {
        return "Velocity " + velocity + " (based on " + estimatedHours + " over " + daysWorked + " days)";
    }
}