package campusfind.service;

import campusfind.dao.ItemDAO;
import campusfind.model.Item;
import campusfind.model.ItemStatus;
import campusfind.model.ItemType;
import campusfind.model.SearchCriteria;
import campusfind.model.User;
import campusfind.util.SecretHasher;
import campusfind.util.Validator;
import java.time.LocalDate;
import java.util.List;

public class ItemService {
    private final ItemDAO itemDAO = new ItemDAO();

    /** Reports a lost or found item. FOUND items need a confidential detail, stored only as a hash. */
    public int reportItem(User reporter, ItemType type, String title, String category, String color,
                          String location, LocalDate date, String description, String secret) {
        if (Validator.isBlank(title) || title.trim().length() > 120) {
            throw new ServiceException("Enter an item name (up to 120 characters).");
        }
        if (Validator.isBlank(category)) {
            throw new ServiceException("Choose a category.");
        }
        if (Validator.isBlank(color) || color.trim().length() > 40) {
            throw new ServiceException("Enter the colour (up to 40 characters).");
        }
        if (Validator.isBlank(location) || location.trim().length() > 120) {
            throw new ServiceException("Enter the location (up to 120 characters).");
        }
        if (date == null || date.isAfter(LocalDate.now())) {
            throw new ServiceException("Enter a valid date (not in the future).");
        }
        String secretHash = null;
        if (type == ItemType.FOUND) {
            if (Validator.isBlank(secret) || secret.trim().length() < 3 || secret.trim().length() > 70) {
                throw new ServiceException("A found item needs a confidential detail (3-70 characters) "
                        + "that only the real owner would know.");
            }
            secretHash = SecretHasher.hash(secret);
        }
        String hash = secretHash;
        return Db.write(c -> itemDAO.insert(c, reporter.id(), type, title.trim(), category, color.trim(),
                location.trim(), date, description == null ? "" : description.trim(), hash));
    }

    public List<Item> search(SearchCriteria criteria) {
        return Db.read(c -> itemDAO.search(c, criteria));
    }

    public List<Item> myItems(int userId) {
        return Db.read(c -> itemDAO.findByReporter(c, userId));
    }

    public List<Item> suggestedMatches(Item lostItem) {
        return Db.read(c -> itemDAO.findMatchesForLost(c, lostItem));
    }

    /** A lost-item report can be closed as recovered by its owner (or an admin). */
    public void markRecovered(User actor, int itemId) {
        Db.exec(c -> {
            Item item = itemDAO.findById(c, itemId).orElseThrow(() -> new ServiceException("Item not found."));
            if (!actor.isAdmin() && item.reporterId() != actor.id()) {
                throw new ServiceException("You can only update your own reports.");
            }
            if (item.type() != ItemType.LOST) {
                throw new ServiceException("Found items are closed through the claim and handover process.");
            }
            itemDAO.updateStatus(c, itemId, ItemStatus.RECOVERED);
        });
    }

    public void deleteItem(User actor, int itemId) {
        Db.exec(c -> {
            Item item = itemDAO.findById(c, itemId).orElseThrow(() -> new ServiceException("Item not found."));
            if (!actor.isAdmin() && item.reporterId() != actor.id()) {
                throw new ServiceException("You can only delete your own reports.");
            }
            if (!actor.isAdmin() && item.status() != ItemStatus.OPEN) {
                throw new ServiceException("This item has a claim in progress or is already recovered.");
            }
            itemDAO.delete(c, itemId);
        });
    }
}
