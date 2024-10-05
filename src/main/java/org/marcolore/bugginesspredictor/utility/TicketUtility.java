package org.marcolore.bugginesspredictor.utility;

import org.marcolore.bugginesspredictor.model.Release;
import org.marcolore.bugginesspredictor.model.Ticket;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TicketUtility {

    public static void checkTicketValidity(ArrayList<Ticket> tickets, ArrayList<Release> releases) {

        tickets.removeIf(ticket -> ticket.getOpeningVersion() == null || ticket.getFixedVersion() == null
                || (ticket.getOpeningVersion().getReleaseDate().isAfter(ticket.getFixedVersion().getReleaseDate()))
                || (ticket.getOpeningVersion().getReleaseDate().isEqual(releases.get(0).getReleaseDate()))
                || (ticket.getOpeningVersion().getIdRelease().equals(releases.get(0).getIdRelease())));

    }


    public static void checkTicketValidityAndCreate(ArrayList<Ticket> tickets, String key, ArrayList<Release> releases, Release injectedVersion, Release openingVersion, Release fixedVersion, LocalDateTime creationDate, LocalDateTime resolutionDate, ArrayList<Release> affectedRelease) {

        if ((openingVersion == null || fixedVersion == null)
                || (openingVersion.getReleaseDate().isAfter(fixedVersion.getReleaseDate()))
                || (openingVersion.getReleaseDate().isEqual(releases.get(0).getReleaseDate()))
                || (openingVersion.getIdRelease().equals(releases.get(0).getIdRelease()))) {
            return;
        }

        if (injectedVersion != null && injectedVersion.getReleaseDate().isAfter(fixedVersion.getReleaseDate())
                || (injectedVersion != null && injectedVersion.getReleaseDate().isAfter(openingVersion.getReleaseDate()))) {
            injectedVersion = null;
            affectedRelease = null;
        }

        tickets.add(new Ticket(key, creationDate, openingVersion, fixedVersion, injectedVersion, resolutionDate, affectedRelease));
    }

    public static void setAV(Ticket ticket, ArrayList<Release> releases) {
        Integer injectedVersion = ticket.getInjectedVersion().getId();
        Integer fixedVersion = ticket.getFixedVersion().getId();
        ticket.setAffectedReleases(new ArrayList<>());
        for (Release release : releases) {
            if (release.getId() >= injectedVersion && release.getId() < fixedVersion) {
                ticket.addAffectedReleases(release);
            }
        }
    }

    public static void setIV(Ticket ticketWithoutIV, ArrayList<Release> releases, float p) {
        int iv;

        // If OV == FV, I assume that FV-OV = 1
        if (Objects.equals(ticketWithoutIV.getOpeningVersion().getId(), ticketWithoutIV.getFixedVersion().getId())
                && ticketWithoutIV.getInjectedVersion() == null) {
            iv = (int) (ticketWithoutIV.getFixedVersion().getId() - p);
        } else {
            // Formula: IV = FV - ((FV - OV) * p)
            iv = (int) (ticketWithoutIV.getFixedVersion().getId()
                    - ((ticketWithoutIV.getFixedVersion().getId() - ticketWithoutIV.getOpeningVersion().getId()) * p));
        }


        if (iv < 1) { //Index not valid
            iv = 1;
        }

        ticketWithoutIV.setInjectedVersion(releases.get(iv - 1));
    }
}


