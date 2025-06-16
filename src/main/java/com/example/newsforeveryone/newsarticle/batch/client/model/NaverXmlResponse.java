package com.example.newsforeveryone.newsarticle.batch.client.model;

import com.example.newsforeveryone.newsarticle.batch.dto.NaverItemDto;
import jakarta.xml.bind.annotation.*;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "rss")
@XmlAccessorType(XmlAccessType.FIELD)
public class NaverXmlResponse {

  @XmlElementWrapper(name = "channel")
  @XmlElement(name = "item")
  private List<NaverItemDto> items;
}