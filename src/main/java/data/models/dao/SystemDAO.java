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
        Query query = manager.createQuery("SELECT q FROM Question q WHERE q.difficulte =:dif");
        query.setParameter("dif", difficulty);
        List<Question> questions = query.getResultList();
        return questions;
    }

    private List<Question> getRandomQuestions(List<Question> list, int N)
    {
        Random rand = new Random();
        // create a temporary list for storing
        // selected element
        List<Question> newList = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            // take a random index between 0 to size
            // of given List
            int randomIndex = rand.nextInt(list.size());
            // add element in temporary list
            newList.add(list.get(randomIndex));
            // Remove selected element from original list
            list.remove(randomIndex);
        }
        return newList;
    }

    @Override
    public Quiz addNewQuiz(String title) {
        return null;
    }

    @Override
    public List<Question> addRandomQuestionsForQuiz(int QuizID, int N, String difficulty) {
        EntityTransaction transaction = manager.getTransaction();
        transaction.begin();
        try {
            Quiz quiz = manager.find(Quiz.class, QuizID);
            if (quiz != null) {
                List<Question> questions = getQuestionsByDifficulty(difficulty);
                if(N > questions.size())
                    N = questions.size();
                List<Question> randomQuestions = getRandomQuestions(questions, N);
                for (Question question : randomQuestions) {
                    QuizQuestionPK quizQuestionPK = new QuizQuestionPK(quiz.getQuizId(),question.getQuestionId());
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
        return null;
    }

    @Override
    public List<Options> optionsForQuestion(int questionId) {

        Query query = manager.createQuery("SELECT o FROM Options o WHERE o.questionId =:qi");
        query.setParameter("qi",  questionId);
        List<Options> options = query.getResultList();

        return options;

    }

    @Override
    public List<Quiz> getNotUsedQuizzes() {
        return null;
    }

    @Override
    public QuizQuestion updateQuizQuestion(int quizId, int questionId, int selectedOptionId) {
        EntityTransaction transaction = manager.getTransaction();
        transaction.begin();
        try {
            QuizQuestionPK quizQuestionPK = new QuizQuestionPK(quizId,questionId);
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
        return null;
    }

    @Override
    public List<Quiz> getUsedQuizzes() {
        return null;
    }

    @Override
    public QuizQuestion getQuizQuestion(int quizId, int questionId) {
        return null;
    }
}
