import javax.persistence.*;
import java.util.Scanner;

public class TreeDelete {
    public static void main(String[] args) {
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("tree");
        EntityManager manager = factory.createEntityManager();

        //Choose category
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите ID категории:");
        Long delInput = Long.parseLong(scanner.nextLine());

        TypedQuery<Long> delQuery = manager.createQuery(
                "select count (c.id) from Tree c where c.id = ?1", Long.class);
        delQuery.setParameter(1, delInput);

        try {
            manager.getTransaction().begin();
            Tree tree = manager.find(Tree.class, delInput);
            Query deleteQuery = manager.createQuery(
                    "DELETE FROM Tree t where t.right <= ?1 and t.left >= ?2 ");
            deleteQuery.setParameter(1, tree.getRight());
            deleteQuery.setParameter(2, tree.getLeft());
            deleteQuery.executeUpdate();

            Query updateLeft = manager.createQuery(
                    "UPDATE Tree t SET t.left = t.left - ?2 where t.left > ?1");
            updateLeft.setParameter(1, tree.getLeft());
            updateLeft.setParameter(2, (tree.getRight()-tree.getLeft())+1);
            updateLeft.executeUpdate();

            Query updateRight = manager.createQuery(
                    "UPDATE Tree t SET t.right = t.right - ?2 where t.right >= ?1");
            updateRight.setParameter(1, tree.getRight());
            updateRight.setParameter(2, (tree.getRight()-tree.getLeft())+1);
            updateRight.executeUpdate();

            manager.getTransaction().commit();
        } catch (Exception e) {
            manager.getTransaction().rollback();
            e.printStackTrace();
        }
    }
}
