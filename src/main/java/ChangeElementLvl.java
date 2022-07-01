import javax.persistence.*;
import java.util.List;
import java.util.Scanner;

public class ChangeElementLvl {
    public static void main(String[] args) {
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("tree");
        EntityManager manager = factory.createEntityManager();

        //Вызов меню

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
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n" + "Введите ID категории:");
        long idInput = Long.parseLong(scanner.nextLine());

        long moveIdInput = Long.parseLong(scanner.nextLine());


        try {
            manager.getTransaction().begin();
            Tree moveTree = manager.find(Tree.class, idInput);


            //преобразование элементов в отрицательные значения

            Query moveQuery = manager.createQuery(
                    "update Tree t set t.left = -t.left, t.right = -t.right where t.left >= ?1 and t.right <=?2");
            moveQuery.setParameter(1, moveTree.getLeft());
            moveQuery.setParameter(2, moveTree.getRight());
            moveQuery.executeUpdate();

            //Освобождение места для левого элемента

            Query updateLeft = manager.createQuery(
                    "UPDATE Tree t SET t.left = t.left - ?2 where t.left > ?1");
            updateLeft.setParameter(1, moveTree.getLeft());
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

                System.out.println(treeId.getRight());
                System.out.println(moveTree.getRight());

                Query KeyMove = manager.createQuery(
                        "UPDATE Tree t SET t.left = (0 - (t.left)) + ?1, t.right = (0 - (t.right)) +?1, t.level = t.level - ?2 + ?3 + 1 where t.left < 0");
                KeyMove.setParameter(1, (treeId.getRight() - moveTree.getRight()) - 1);
                KeyMove.setParameter(2, moveTree.getLevel());
                KeyMove.setParameter(3, treeId.getLevel());
                KeyMove.executeUpdate();
                manager.refresh(treeId);

//                level - move_level + parent_level + 1


            }
            manager.getTransaction().commit();
        } catch (Exception e) {
            manager.getTransaction().rollback();
            e.printStackTrace();

        }

    }
}


//level - moveTree.level + parentTree.level + 1
// Перемещаемый основной элемент: Intel (3, 4) -> Intel (7, 8)
// Родительский элемент: ОЗУ (6, 9)

// 0 - левый ключ перемещаемого + (правый ключ родительского - правый ключ перемещаемого основного - 1)
// 0 - (-3) + (9 - 4 - 1) = 7

// 0 - правый ключ перемещаемого + (правый ключ родительского - правый ключ перемещаемого основного - 1)
// 0 - (-4) + (9 - 4 - 1) = 8

// Процессоры (2, 7) -> Процессоры (3, 8)
// - Intel (3, 4)       - Intel (4, 5)
// - AMD (5, 6)         - AMD (6, 7)

// ОЗУ (2, 9)

// 0 - (-2) + (9 - 7 - 1) = 3
// 0 - (-7) + (9 - 7 - 1) = 8
// 0 - (-3) + (9 - 7 - 1) = 4
// 0 - (-4) + (9 - 7 - 1) = 5
// 0 - (-5) + (9 - 7 - 1) = 6
// 0 - (-6) + (9 - 7 - 1) = 7


// Homework если переносим элемент на лвл 0, то создается новый родительский с лвл 0