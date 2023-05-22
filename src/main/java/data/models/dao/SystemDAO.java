package data.models.dao;

import data.models.entities.*;
import jakarta.persistence.*;
import java.util.*;

public class SystemDAO implements ISystemDAO {
    EntityManagerFactory factory = null;
    EntityManager manager = null;

    public SystemDAO() {
        factory = Persistence.createEntityManagerFactory("quiz_unit");
        manager = factory.createEntityManager();
    }

    private List<Question> getQuestionsByDifficulty(String difficulty) {
        Query query = manager.createQuery("SELECT q FROM Question q WHERE q.difficulte = :difficulty");
        query.setParameter("difficulty", difficulty);
        List<Question> questions = query.getResultList();
        return questions;
    }

    private List<Question> getRandomQuestions(List<Question> list, int N) {
        Random rand = new Random();
        List<Question> newList = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            int randomIndex = rand.nextInt(list.size());
            newList.add(list.get(randomIndex));
            list.remove(randomIndex);
        }
        return newList;
    }

    @Override
    public Quiz addNewQuiz(String title) {
        EntityTransaction transaction = manager.getTransaction();
        transaction.begin();
        try {
            Quiz quiz = new Quiz();
            quiz.setTitre(title);
            manager.persist(quiz);
            transaction.commit();
            return quiz;
        } catch (Exception e) {
            transaction.rollback();
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public List<Question> addRandomQuestionsForQuiz(int quizId, int N, String difficulty) {
        EntityTransaction transaction = manager.getTransaction();
        transaction.begin();
        try {
            Quiz quiz = manager.find(Quiz.class, quizId);
            if (quiz != null) {
                List<Question> questions = getQuestionsByDifficulty(difficulty);
                if (N > questions.size()) {
                    N = questions.size();
                }
                List<Question> randomQuestions = getRandomQuestions(questions, N);
                for (Question question : randomQuestions) {
                    QuizQuestionPK quizQuestionPK = new QuizQuestionPK(quizId, question.getQuestionId());
                    QuizQuestion quizQuestion = new QuizQuestion(quizQuestionPK, 0, quiz, question);
                    manager.persist(quizQuestion);
                }
                transaction.commit();
                return randomQuestions;
            }
        } catch (Exception e) {
            transaction.rollback();
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public List<Question> getQuestionsForQuiz(int quizId) {
        EntityTransaction transaction = EntityManagerSingleton.getInstance().manager.getTransaction();
        List<Question> questions = new ArrayList<>();

        try {
            transaction.begin();
            Query query = EntityManagerSingleton.getInstance().manager.createQuery("SELECT qq.questionByQuestionId FROM QuizQuestion qq WHERE qq.quizByQuizId.quizId = :quizId");
            query.setParameter("quizId", quizId);
            questions = query.getResultList();
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println(e.getMessage());
        }
        return questions;
    }

    @Override
    public List<Options> optionsForQuestion(int questionId) {
        Query query = manager.createQuery("SELECT o FROM Options o WHERE o.questionId = :questionId");
        query.setParameter("questionId", questionId);
        List<Options> options = query.getResultList();
        return options;
    }

    @Override
    public List<Quiz> getNotUsedQuizzes() {
        Query query = manager.createQuery("SELECT q FROM Quiz q WHERE NOT EXISTS (SELECT qq FROM QuizQuestion qq WHERE qq.quizByQuizId = q)");
        List<Quiz> quizzes = query.getResultList();
        return quizzes;
    }

    @Override
    public List<Quiz> getUsedQuizzes() {
        EntityTransaction transaction = EntityManagerSingleton.getInstance().manager.getTransaction();
        List<Quiz> quizzes = new ArrayList<>();

        try {
            transaction.begin();
            Query query = EntityManagerSingleton.getInstance().manager.createQuery("SELECT q FROM Quiz q WHERE EXISTS (SELECT qq FROM QuizQuestion qq WHERE qq.quizByQuizId = q)");
            quizzes = query.getResultList();
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println(e.getMessage());
        }
        return quizzes;
    }


    @Override
    public QuizQuestion updateQuizQuestion(int quizId, int questionId, int selectedOptionId) {
        EntityTransaction transaction = manager.getTransaction();
        transaction.begin();
        try {
            QuizQuestionPK quizQuestionPK = new QuizQuestionPK(quizId, questionId);
            QuizQuestion quizQuestion = manager.find(QuizQuestion.class, quizQuestionPK);
            if (quizQuestion != null) {
                quizQuestion.setSelectedOptionID(selectedOptionId);
                transaction.commit();
                return quizQuestion;
            }
        } catch (Exception e) {
            transaction.rollback();
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public Options rightOptionsForQuestion(int questionId) {
        EntityTransaction transaction = EntityManagerSingleton.getInstance().manager.getTransaction();
        Options correctOption = null;

        try {
            transaction.begin();
            Query query = EntityManagerSingleton.getInstance().manager.createQuery("SELECT o FROM Options o WHERE o.questionId = :questionId AND o.estVrai = true");
            query.setParameter("questionId", questionId);
            List<Options> options = query.getResultList();
            if (!options.isEmpty()) {
                correctOption = options.get(0);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println(e.getMessage());
        }
        return correctOption;
    }


    @Override
    public QuizQuestion getQuizQuestion(int quizId, int questionId) {
        QuizQuestionPK quizQuestionPK = new QuizQuestionPK(quizId, questionId);
        QuizQuestion quizQuestion = manager.find(QuizQuestion.class, quizQuestionPK);
        return quizQuestion;
    }
}
