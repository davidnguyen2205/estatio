package org.estatio.dom.lease;

import java.math.BigInteger;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.VersionStrategy;

import org.estatio.dom.agreement.Agreement;
import org.estatio.dom.invoice.InvoiceProvenance;
import org.estatio.dom.lease.Leases.InvoiceRunType;
import org.estatio.dom.party.Party;
import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.Bookmarkable;
import org.apache.isis.applib.annotation.Bulk;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.NotPersisted;
import org.apache.isis.applib.annotation.Prototype;
import org.apache.isis.applib.annotation.Render;
import org.apache.isis.applib.annotation.Render.Type;

@javax.jdo.annotations.PersistenceCapable
@javax.jdo.annotations.Inheritance(strategy = InheritanceStrategy.SUPERCLASS_TABLE)
@javax.jdo.annotations.Discriminator(strategy = DiscriminatorStrategy.CLASS_NAME)
@javax.jdo.annotations.Version(strategy = VersionStrategy.VERSION_NUMBER, column = "VERSION")
@javax.jdo.annotations.Queries({ @javax.jdo.annotations.Query(name = "lease_findLeaseByReference", language = "JDOQL", value = "SELECT FROM org.estatio.dom.lease.Lease WHERE reference.matches(:r) && (terminationDate == null || terminationDate >= :date)") })
@Bookmarkable
public class Lease extends Agreement implements InvoiceProvenance {

    @NotPersisted
    @MemberOrder(sequence = "3")
    public Party getPrimaryParty() {
        return findParty(LeaseConstants.ART_LANDLORD);
    }

    @NotPersisted
    @MemberOrder(sequence = "4")
    public Party getSecondaryParty() {
        return findParty(LeaseConstants.ART_TENANT);
    }

    // //////////////////////////////////////

    private LeaseType type;

    @MemberOrder(sequence = "8")
    public LeaseType getType() {
        return type;
    }

    public void setType(final LeaseType type) {
        this.type = type;
    }

    // //////////////////////////////////////

    @javax.jdo.annotations.Persistent(mappedBy = "lease")
    private SortedSet<LeaseUnit> units = new TreeSet<LeaseUnit>();

    @MemberOrder(name = "Units", sequence = "20")
    @Render(Type.EAGERLY)
    public SortedSet<LeaseUnit> getUnits() {
        return units;
    }

    public void setUnits(final SortedSet<LeaseUnit> units) {
        this.units = units;
    }

    public void addToUnits(final LeaseUnit leaseUnit) {
        if (leaseUnit == null || getUnits().contains(leaseUnit)) {
            return;
        }
        leaseUnit.clearLease();
        leaseUnit.setLease(this);
        getUnits().add(leaseUnit);
    }

    public void removeFromUnits(final LeaseUnit leaseUnit) {
        if (leaseUnit == null || !getUnits().contains(leaseUnit)) {
            return;
        }
        leaseUnit.setLease(null);
        getUnits().remove(leaseUnit);
    }

    @MemberOrder(name = "Units", sequence = "21")
    public LeaseUnit addUnit(@Named("unit") UnitForLease unit) {
        LeaseUnit leaseUnit = leaseUnits.newLeaseUnit(this, unit);
        units.add(leaseUnit);
        return leaseUnit;
    }

    // //////////////////////////////////////

    @javax.jdo.annotations.Persistent(mappedBy = "lease")
    private SortedSet<LeaseItem> items = new TreeSet<LeaseItem>();

    @Render(Type.EAGERLY)
    @MemberOrder(name = "Items", sequence = "30")
    public SortedSet<LeaseItem> getItems() {
        return items;
    }

    public void setItems(final SortedSet<LeaseItem> items) {
        this.items = items;
    }

    public void addToItems(final LeaseItem leaseItem) {
        if (leaseItem == null || getItems().contains(leaseItem)) {
            return;
        }
        leaseItem.clearLease();
        leaseItem.setLease(this);
        getItems().add(leaseItem);
    }

    public void removeFromItems(final LeaseItem leaseItem) {
        if (leaseItem == null || !getItems().contains(leaseItem)) {
            return;
        }
        leaseItem.setLease(null);
        getItems().remove(leaseItem);
    }

    @MemberOrder(name = "Items", sequence = "31")
    public LeaseItem newItem(LeaseItemType type) {
        LeaseItem leaseItem = leaseItems.newLeaseItem(this, type);
        return leaseItem;
    }

    @Hidden
    public LeaseItem findItem(LeaseItemType type, LocalDate startDate, BigInteger sequence) {
        // TODO: better/faster filter options? -> Use predicate
        for (LeaseItem item : getItems()) {
            LocalDate itemStartDate = item.getStartDate();
            LeaseItemType itemType = item.getType();
            if (itemType.equals(type) && itemStartDate.equals(startDate) && item.getSequence().equals(sequence)) {
                return item;
            }
        }
        return null;
    }

    @Hidden
    public LeaseItem findFirstItemOfType(LeaseItemType type) {
        for (LeaseItem item : getItems()) {
            if (item.getType().equals(type)) {
                return item;
            }
        }
        return null;
    }

    // //////////////////////////////////////

    @Bulk
    @Prototype
    public Lease approveAllTermsOfThisLease() {
        for (LeaseItem item : getItems()) {
            for (LeaseTerm term : item.getTerms()) {
                term.approve();
            }
        }
        return this;
    }

    @Bulk
    public Lease verify() {
        for (LeaseItem item : getItems()) {
            item.verify();
        }
        return this;
    }

    @Bulk
    public Lease calculate(@Named("Period Start Date") LocalDate startDate, @Named("Due date") LocalDate dueDate, @Named("Run Type") InvoiceRunType runType) {
        // TODO: I know that bulk actions only appear whith a no-arg but why
        // not?
        for (LeaseItem item : getItems()) {
            item.calculate(startDate, dueDate, runType);
        }
        return this;
    }

    // //////////////////////////////////////

    private LeaseItems leaseItems;

    public void injectLeaseItems(final LeaseItems leaseItems) {
        this.leaseItems = leaseItems;
    }

    private LeaseUnits leaseUnits;

    public void injectLeaseUnits(final LeaseUnits leaseUnits) {
        this.leaseUnits = leaseUnits;
    }

}
