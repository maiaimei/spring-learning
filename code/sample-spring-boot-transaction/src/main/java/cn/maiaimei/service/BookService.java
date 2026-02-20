package cn.maiaimei.service;

import cn.maiaimei.model.Book;
import cn.maiaimei.repository.BookRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BookService {

  @Autowired
  private BookRepository bookRepository;

  @Transactional(readOnly = true)
  public List<Book> getAllBooks() {
    return bookRepository.findAll();
  }

  @Transactional(readOnly = true)
  public Book getBookById(Long id) {
    return bookRepository.findById(id);
  }

  public Book saveBook(Book book) {
    bookRepository.save(book);
    return book;
  }

  public Book updateBook(Book book) {
    bookRepository.update(book);
    return book;
  }

  public void deleteBook(Long id) {
    bookRepository.deleteById(id);
  }
}
