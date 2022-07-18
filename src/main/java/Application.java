import javax.persistence.*;
import java.util.List;
import java.util.Scanner;
import entity.Tree;


public class Application {

    private static final Scanner IN = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Меню");
        System.out.println("[1] - Add new Entity.Tree");
        System.out.println("[2] - Update");
        System.out.println("[3] - Delete");

        int select = Integer.parseInt(IN.nextLine());

        switch (select){
            case 1:
                treeAdd();
                break;
            case 2:
                changeElementLvl();
                break;
            case 3:
                delete();
                break;
            default:
                System.out.println("Введите корректное значение");
                break;
        }


    }

    private static EntityManagerFactory FACTORY = Persistence.createEntityManagerFactory("tree");

    private static void treeAdd(){
        EntityManager manager = FACTORY.createEntityManager();

        TypedQuery<Tree> treeQuery = manager.createQuery(
                "select t from Tree t order by t.left", Tree.class);
        List<Tree> trees = treeQuery.getResultList();
        for (Tree tree : trees) {
            String dash = "";
            for (int i = 0; i < tree.getLevel(); i++) {
                dash += "-";
            }
            System.out.println(dash + tree.getName() + " [" + tree.getId() + "]");

        }

        System.out.println("\n" + "Введите ID категории:");
        long idInput = Long.parseLong(IN.nextLine());

        TypedQuery<Long> treeCountQuery = manager.createQuery(
                "select count (c.id) from Tree c where c.id =?1", Long.class);
        treeCountQuery.setParameter(1, idInput);
        long idCount = treeCountQuery.getSingleResult();
        if (idInput == 0) {
            TypedQuery<Integer> queryLvl = manager.createQuery(
                    "select max(t.right) from Tree t", Integer.class);
            int maxRight = queryLvl.getSingleResult();
            System.out.println("Max right: " + maxRight);
            String newNameLvl = IN.nextLine();

            try {
                manager.getTransaction().begin();

                Tree newLvlTree = new Tree();
                newLvlTree.setName(newNameLvl);
                newLvlTree.setLevel(0);
                newLvlTree.setLeft(maxRight + 1);
                newLvlTree.setRight(maxRight + 2);
                manager.persist(newLvlTree);

                manager.getTransaction().commit();
            } catch (Exception e) {
                manager.getTransaction().rollback();
                e.printStackTrace();
            }

        } else {
            while (idCount == 0) {
                System.out.println("\n" + "Данного ID нет");
                System.out.println("\n" + "Введите ID категории:");

                idInput = Long.parseLong(IN.nextLine());
                treeCountQuery = manager.createQuery(
                        "select count (c.id) from Tree c where c.id =?1", Long.class);
                treeCountQuery.setParameter(1, idInput);
                idCount = treeCountQuery.getSingleResult();
            }

            System.out.println("Введите название:");
            String nameOfCat = IN.nextLine();

            try {
                manager.getTransaction().begin();
                Tree tree = manager.find(Tree.class, idInput);
                Query leftQuery = manager.createQuery(
                        "UPDATE Tree t SET t.left = t.left + 2  where t.left > ?1");
                leftQuery.setParameter(1, tree.getRight());
                leftQuery.executeUpdate();

                Query rightQuery = manager.createQuery(
                        "UPDATE Tree t SET t.right = t.right + 2 where t.right >= ?1");
                rightQuery.setParameter(1, tree.getRight());
                rightQuery.executeUpdate();


                Tree newTree = new Tree();

                newTree.setLevel(tree.getLevel() + 1);
                newTree.setName(nameOfCat);
                newTree.setLeft(tree.getRight());
                newTree.setRight(tree.getRight() + 1);
                manager.persist(newTree);


                manager.getTransaction().commit();
            } catch (Exception e) {
                manager.getTransaction().rollback();
                e.printStackTrace();
            }finally {
                manager.close();
            }
        }
    }

    private static void changeElementLvl(){
        EntityManager manager = FACTORY.createEntityManager();

        try {

            TypedQuery<Tree> treeQuery = manager.createQuery(
                    "select t from Tree t order by t.left", Tree.class);
            List<Tree> trees = treeQuery.getResultList();
            for (Tree tree : trees) {
                String dash = "";
                for (int i = 0; i < tree.getLevel(); i++) {
                    dash += "-";
                }
                System.out.println(dash + tree.getName() + " [" + tree.getId() + "]");

            }

            System.out.println("\n" + "Введите ID категории:");
            long idInput = Long.parseLong(IN.nextLine());

            long moveIdInput = Long.parseLong(IN.nextLine());
            manager.getTransaction().begin();
            Tree moveTree = manager.find(Tree.class, idInput);


            //преобразование элементов в отрицательные значения

            Query moveQuery = manager.createQuery(
                    "update Tree t set t.left = -t.left, t.right = -t.right where t.left >= ?1 and t.right <=?2");
            moveQuery.setParameter(1, moveTree.getLeft());
            moveQuery.setParameter(2, moveTree.getRight());
            moveQuery.executeUpdate();

            //Освобождение места для левого элемента
//1
            Query updateLeft = manager.createQuery(
                    "UPDATE Tree t SET t.left = t.left - ?2 where t.left > ?1");
            updateLeft.setParameter(1, moveTree.getRight());
            updateLeft.setParameter(2, (moveTree.getRight() - moveTree.getLeft()) + 1);
            updateLeft.executeUpdate();


            //Освобождение места для правого элемента

            Query updateRight = manager.createQuery(
                    "UPDATE Tree t SET t.right = t.right - ?2 where t.right >= ?1");
            updateRight.setParameter(1, moveTree.getRight());
            updateRight.setParameter(2, (moveTree.getRight() - moveTree.getLeft()) + 1);
            updateRight.executeUpdate();

            //Условие при выборе 0-го ID


            if (moveIdInput == 0) {
                TypedQuery<Integer> maxR = manager.createQuery(
                        "select max(t.right) from Tree t", Integer.class);
                int maxRight = maxR.getSingleResult();

                Query zeroLvl = manager.createQuery("UPDATE Tree t SET t.left = -t.left - ?1 + ?2 + 2 , t.right = -t.right - ?1 + ?2 + 2, t.level = t.level -?3 where t.left < 0");
                zeroLvl.setParameter(1, moveTree.getRight());
                zeroLvl.setParameter(2, maxRight);
                zeroLvl.setParameter(3, moveTree.getLevel());

                zeroLvl.executeUpdate();

//-left - move_left + parent_right + 1
//
//-right - move_left + parent_right + 1

            } else {
                Tree treeId = manager.find(Tree.class, moveIdInput);
                manager.refresh(treeId);

                Query leftQuery = manager.createQuery(
                        "UPDATE Tree t SET t.left = t.left + ?1  where t.left > ?2");
                leftQuery.setParameter(1, (moveTree.getRight() - moveTree.getLeft()) + 1);
                leftQuery.setParameter(2, treeId.getRight());
                leftQuery.executeUpdate();

                Query rightQuery = manager.createQuery(
                        "UPDATE Tree t SET t.right = t.right + ?1 where t.right >= ?2");
                rightQuery.setParameter(1, (moveTree.getRight() - moveTree.getLeft()) + 1);
                rightQuery.setParameter(2, treeId.getRight());
                rightQuery.executeUpdate();

                manager.refresh(treeId);

                Query KeyMove = manager.createQuery(
                        "UPDATE Tree t SET t.left = (0 - (t.left)) + ?1, t.right = (0 - (t.right)) +?1, t.level = t.level - ?2 + ?3 + 1 where t.left < 0");
                KeyMove.setParameter(1, (treeId.getRight() - moveTree.getRight()) - 1);
                KeyMove.setParameter(2, moveTree.getLevel());
                KeyMove.setParameter(3, treeId.getLevel());
                KeyMove.executeUpdate();
                manager.refresh(treeId);

            }

            manager.getTransaction().commit();
        } catch (Exception e) {
            manager.getTransaction().rollback();
            e.printStackTrace();

        }finally {
            manager.close();
        }

    }

    private static void delete(){
        EntityManager manager = FACTORY.createEntityManager();

        try {
            TypedQuery<Tree> treeQuery = manager.createQuery(
                    "select t from Tree t order by t.left", Tree.class);
            List<Tree> trees = treeQuery.getResultList();
            for (Tree tree : trees) {
                String dash = "";
                for (int i = 0; i < tree.getLevel(); i++) {
                    dash += "-";
                }
                System.out.println(dash + tree.getName() + " [" + tree.getId() + "]");

            }

            System.out.println("Введите ID категории:");
            Long delInput = Long.parseLong(IN.nextLine());

            TypedQuery<Long> delQuery = manager.createQuery(
                    "select count (c.id) from Tree c where c.id = ?1", Long.class);
            delQuery.setParameter(1, delInput);
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
        }finally {
            manager.close();
        }

    }


}
